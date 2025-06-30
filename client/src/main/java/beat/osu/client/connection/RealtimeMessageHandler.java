package beat.osu.client.connection;

import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import beat.osu.shared.models.RealtimeMessage;

public class RealtimeMessageHandler {
    private final ObjectOutputStream oos;
    private final Object writeLock;
    private final List<RealtimeMessageCallback> callbacks = new CopyOnWriteArrayList<>();
    
    // BlockingQueue for managing outgoing messages
    private final BlockingQueue<RealtimeMessage> messageQueue = new LinkedBlockingQueue<>(1000);
    private final Thread senderThread;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    private int messagesSent = 0;
    private static final int RESET_INTERVAL = 100; // Reset stream every 100 messages

    public RealtimeMessageHandler(ObjectOutputStream oos, Object writeLock) {
        this.oos = oos;
        this.writeLock = writeLock;
        this.senderThread = new Thread(this::processMessageQueue, "RealtimeMessage-Sender");
        this.senderThread.setDaemon(true);
        start();
    }

    /**
     * Starts the message processing thread
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            senderThread.start();
            System.out.println("RealtimeMessageHandler started");
        }
    }

    /**
     * Stops the message processing thread and clears the queue
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            senderThread.interrupt();
            messageQueue.clear();
            System.out.println("RealtimeMessageHandler stopped");
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
                System.err.println("Error processing message from queue: " + e.getMessage());
                // Continue processing other messages
            }
        }
    }

    /**
     * Actually sends the message through the ObjectOutputStream
     */
    private void sendMessageDirectly(RealtimeMessage message) {
        try {
            synchronized (writeLock) {
                oos.writeObject(message);
                oos.flush();

                // Reset ObjectOutputStream periodically to prevent stream corruption
                messagesSent++;
                if (messagesSent >= RESET_INTERVAL) {
                    oos.reset();
                    messagesSent = 0;
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending realtime message: " + e.getMessage());
        }
    }

    public interface RealtimeMessageCallback {
        void onRealtimeMessage(RealtimeMessage message);
    }

    public void addCallback(RealtimeMessageCallback callback) {
        callbacks.add(callback);
    }

    public void removeCallback(RealtimeMessageCallback callback) {
        callbacks.remove(callback);
    }

    public void sendRealtimeMessage(RealtimeMessage message) {
        if (!isRunning.get()) {
            System.err.println("RealtimeMessageHandler is not running, cannot send message");
            return;
        }

        try {
            // Non-blocking offer to prevent indefinite blocking
            boolean offered = messageQueue.offer(message);
            if (!offered) {
                // Queue is full - implement drop strategy
                handleQueueOverflow(message);
            }
        } catch (Exception e) {
            System.err.println("Error queuing realtime message: " + e.getMessage());
        }
    }

    /**
     * Handles queue overflow by implementing a drop strategy
     */
    private void handleQueueOverflow(RealtimeMessage newMessage) {
        // Strategy 1: Drop oldest message and add new one (FIFO)
        RealtimeMessage droppedMessage = messageQueue.poll();
        if (droppedMessage != null) {
            boolean offered = messageQueue.offer(newMessage);
            if (offered) {
                System.err.println("Queue was full, dropped oldest message to make room for new one");
            } else {
                System.err.println("Failed to add message even after dropping oldest - queue might be blocked");
            }
        } else {
            System.err.println("Message queue is full and unable to drop messages");
        }
        
        // Alternative strategies you could implement:
        // Strategy 2: Drop the new message (uncomment below)
        // System.err.println("Message queue is full, dropping new message");
        
        // Strategy 3: Clear half the queue (uncomment below)
        // int halfSize = messageQueue.size() / 2;
        // for (int i = 0; i < halfSize; i++) {
        //     messageQueue.poll();
        // }
        // messageQueue.offer(newMessage);
    }

    /**
     * Gets the current queue size for monitoring purposes
     * @return the number of messages waiting to be sent
     */
    public int getQueueSize() {
        return messageQueue.size();
    }

    /**
     * Checks if the message handler is currently running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    public void handleIncomingMessage(RealtimeMessage message) {
        for (RealtimeMessageCallback callback : callbacks) {
            try {
                callback.onRealtimeMessage(message);
            } catch (Exception e) {
                System.err.println("Error in realtime message callback: " + e.getMessage());
            }
        }
    }
}
