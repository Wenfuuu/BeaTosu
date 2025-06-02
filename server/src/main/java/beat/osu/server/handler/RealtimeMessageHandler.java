package beat.osu.server.handler;

import beat.osu.shared.models.RealtimeMessage;
import beat.osu.shared.enums.RealtimeMessageType;

import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

public class RealtimeMessageHandler {
    private final ObjectOutputStream outputStream;
    private final String clientId;

    private static final ConcurrentHashMap<String, RealtimeMessageHandler> activeHandlers = new ConcurrentHashMap<>();

    public RealtimeMessageHandler(ObjectOutputStream outputStream, String clientId) {
        this.outputStream = outputStream;
        this.clientId = clientId;
        activeHandlers.put(clientId, this);
        
        broadcastUserCount();
    }

    public void handleRealtimeMessage(RealtimeMessage message, String fromClientId) {
        try {
            message.setFromClientId(fromClientId);
            message.setTimestamp(System.currentTimeMillis());

            switch (message.getType()) {
                case CHAT_MESSAGE:
                    broadcastToAllExcept(message, fromClientId);
                    break;
                case USER_JOINED:
                case USER_LEFT:
                    broadcastToAll(message);
                    break;
                case SYSTEM_NOTIFICATION:
                    // System notifications go to all clients
                    broadcastToAll(message);
                    break;
                default:
                    System.out.println("RealtimeMessageHandler: Unknown message type: " + message.getType());
                    return;
            }

        } catch (Exception e) {
            System.err.println("RealtimeMessageHandler: Error handling message: " + e.getMessage());
        }
    }

    public void sendToClient(RealtimeMessage message, String targetClientId) {
        RealtimeMessageHandler targetHandler = activeHandlers.get(targetClientId);
        if (targetHandler != null) {
            try {
                targetHandler.outputStream.writeObject(message);
                targetHandler.outputStream.flush();
            } catch (Exception e) {
                System.err.println("RealtimeMessageHandler: Error sending to client " + targetClientId + ": " + e.getMessage());
                activeHandlers.remove(targetClientId);
            }
        } else {
            System.out.println("RealtimeMessageHandler: Target client " + targetClientId + " not found");
        }
    }

    public void broadcastToAllExcept(RealtimeMessage message, String excludeClientId) {
        for (String clientId : activeHandlers.keySet()) {
            if (!clientId.equals(excludeClientId)) {
                sendToClient(message, clientId);
            }
        }
    }

    public void broadcastToAll(RealtimeMessage message) {
        for (String clientId : activeHandlers.keySet()) {
            sendToClient(message, clientId);
        }
    }

    public void cleanup() {
        activeHandlers.remove(clientId);

        RealtimeMessage userLeftMessage = new RealtimeMessage(RealtimeMessageType.USER_LEFT, "SYSTEM", null);
        broadcastToAllExcept(userLeftMessage, clientId);
        
        broadcastUserCount();
    }

    public static int getActiveClientCount() {
        return activeHandlers.size();
    }

    public static void broadcastUserCount() {
        int userCount = activeHandlers.size();
        RealtimeMessage userCountMessage = new RealtimeMessage(RealtimeMessageType.USER_COUNT_UPDATE, "SYSTEM", userCount);

        for (RealtimeMessageHandler handler : activeHandlers.values()) {
            try {
                handler.outputStream.writeObject(userCountMessage);
                handler.outputStream.flush();
            } catch (Exception e) {
                System.err.println("RealtimeMessageHandler: Error sending user count: " + e.getMessage());
            }
        }
    }

    public static void sendSystemNotification(String message) {
        RealtimeMessage notification = new RealtimeMessage(RealtimeMessageType.SYSTEM_NOTIFICATION, "SYSTEM", message);

        for (RealtimeMessageHandler handler : activeHandlers.values()) {
            try {
                handler.outputStream.writeObject(notification);
                handler.outputStream.flush();
            } catch (Exception e) {
                System.err.println("RealtimeMessageHandler: Error sending system notification: " + e.getMessage());
            }
        }
    }
}
