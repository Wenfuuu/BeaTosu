package beat.osu.server.handler;

import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import beat.osu.shared.enums.message.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;

public class RealtimeMessageHandler {
    private final ObjectOutputStream outputStream;
    private final String clientId;
    
    private final BlockingQueue<RealtimeMessage> messageQueue = new LinkedBlockingQueue<>(5000); // Increased for high throughput
    private final Thread senderThread;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private static final ConcurrentHashMap<String, RealtimeMessageHandler> activeHandlers = new ConcurrentHashMap<>();

    public RealtimeMessageHandler(ObjectOutputStream outputStream, String clientId) {
        this.outputStream = outputStream;
        this.clientId = clientId;
        this.senderThread = new Thread(this::processMessageQueue, "RealtimeMessage-Sender-" + clientId);
        this.senderThread.setDaemon(true);
        
        activeHandlers.put(clientId, this);
        start();
    }

    /**
     * Starts the message processing thread
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            senderThread.start();
        }
    }

    /**
     * Stops the message processing thread and clears the queue
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            senderThread.interrupt();
            messageQueue.clear();
        }
    }

    /**
     * Background thread that processes messages from the queue
     */
    private void processMessageQueue() {
        while (isRunning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                // This will block until a message is available
                RealtimeMessage message = messageQueue.take();
                sendMessageDirectly(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error processing message from queue for client " + clientId + ": " + e.getMessage());
                // Continue processing other messages
            }
        }
    }

    /**
     * Actually sends the message through the ObjectOutputStream
     */
    private void sendMessageDirectly(RealtimeMessage message) {
        try {
            synchronized (outputStream) {
                outputStream.writeObject(message);
                outputStream.flush();
                // Reset stream much less frequently for high-throughput scenarios
                messagesSent++;
                if (messagesSent >= 100) { // Reset every 100 messages instead of every message
                    outputStream.reset();
                    messagesSent = 0;
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending realtime message to client " + clientId + ": " + e.getMessage());
            // Remove this handler as the connection is likely broken
            activeHandlers.remove(clientId);
        }
    }
    
    private int messagesSent = 0;

    /**
     * Queues a message to be sent to this specific client
     */
    private void queueMessage(RealtimeMessage message) {
        if (!isRunning.get()) {
            System.err.println("RealtimeMessageHandler for client " + clientId + " is not running, cannot send message");
            return;
        }

        try {
            boolean offered = messageQueue.offer(message);
            if (!offered) {
                // Handle queue overflow
                RealtimeMessage droppedMessage = messageQueue.poll();
                if (droppedMessage != null) {
                    messageQueue.offer(message);
                    System.err.println("Queue full for client " + clientId + ", dropped oldest message");
                } else {
                    System.err.println("Failed to queue message for client " + clientId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error queuing message for client " + clientId + ": " + e.getMessage());
        }
    }

    public void handleRealtimeMessage(RealtimeMessage message, String fromClientId) {
        try {
            message.setFromClientId(fromClientId);
            message.setTimestamp(System.currentTimeMillis());

            switch (message.getType()) {
                case SYSTEM_NOTIFICATION:
                    broadcastToAll(message);
                    break;
                default:
            }

        } catch (Exception e) {
            System.err.println("RealtimeMessageHandler: Error handling message: " + e.getMessage());
        }
    }

    public static void sendToClient(RealtimeMessage message, String targetClientId) {
        RealtimeMessageHandler targetHandler = activeHandlers.get(targetClientId);
        if (targetHandler != null) {
            targetHandler.queueMessage(message);
        } else {
            // System.out.println("RealtimeMessageHandler: Target client " + targetClientId + " not found");
        }
    }

    public static void broadcastToAllExcept(RealtimeMessage message, String excludeClientId) {
        for (String clientId : activeHandlers.keySet()) {
            if (!clientId.equals(excludeClientId)) {
                sendToClient(message, clientId);
            }
        }
    }

    public static void broadcastToAll(RealtimeMessage message) {
        for (String clientId : activeHandlers.keySet()) {
            sendToClient(message, clientId);
        }
    }

    public void cleanup() {
        // Stop the message processing thread first
        stop();
        
        // Remove from active handlers
        activeHandlers.remove(clientId);

        // Broadcast user left message
        RealtimeMessage userLeftMessage = new RealtimeMessage(RealtimeMessageType.USER_DISCONNECTED, "SYSTEM", null);
        broadcastToAllExcept(userLeftMessage, clientId);
    }

    /**
     * Gets the current queue size for monitoring purposes
     */
    public int getQueueSize() {
        return messageQueue.size();
    }

    /**
     * Checks if the message handler is currently running
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    /**
     * Gets statistics for all active handlers (useful for monitoring)
     */
    public static String getGlobalStats() {
        StringBuilder stats = new StringBuilder("Active Handlers: " + activeHandlers.size() + "\n");
        for (RealtimeMessageHandler handler : activeHandlers.values()) {
            stats.append("Client ").append(handler.clientId)
                 .append(": Queue Size=").append(handler.getQueueSize())
                 .append(", Running=").append(handler.isRunning())
                 .append("\n");
        }
        return stats.toString();
    }

    public static void sendSystemNotification(String message) {
        RealtimeMessage notification = new RealtimeMessage(RealtimeMessageType.SYSTEM_NOTIFICATION, "SYSTEM", message);
        broadcastToAll(notification);
    }
}
