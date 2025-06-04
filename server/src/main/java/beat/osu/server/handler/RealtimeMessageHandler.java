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
                    System.out.println("RealtimeMessageHandler: Unknown message type: " + message.getType());
            }

        } catch (Exception e) {
            System.err.println("RealtimeMessageHandler: Error handling message: " + e.getMessage());
        }
    }

    public static void sendToClient(RealtimeMessage message, String targetClientId) {
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

    public static void broadcastToAll(RealtimeMessage message) {
        for (String clientId : activeHandlers.keySet()) {
            sendToClient(message, clientId);
        }
    }

    public void cleanup() {
        activeHandlers.remove(clientId);

        RealtimeMessage userLeftMessage = new RealtimeMessage(RealtimeMessageType.USER_DISCONNECTED, "SYSTEM", null);
        broadcastToAllExcept(userLeftMessage, clientId);
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
