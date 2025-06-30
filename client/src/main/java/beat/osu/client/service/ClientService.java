package beat.osu.client.service;

import beat.osu.client.connection.ServerConnection;
import beat.osu.shared.models.RealtimeMessage;
import lombok.Getter;

@Getter
public class ClientService {
    private static volatile ClientService instance;
    private final ServerConnection connection;

    private ClientService() {
        this.connection = new ServerConnection();
    }

    public static ClientService getInstance() {
        if (instance == null) {
            synchronized (ClientService.class) {
                if (instance == null) {
                    instance = new ClientService();
                }
            }
        }
        return instance;
    }

    public boolean connect() {
        return connection.connect();
    }

    public void disconnect() {
        connection.disconnect();
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    /**
     * Sends a realtime message to the server
     */
    public void sendRealtimeMessage(RealtimeMessage message) {
        connection.sendRealtimeMessage(message);
    }

    /**
     * Gets the current queue size for monitoring
     */
    public int getMessageQueueSize() {
        if (connection.getRealtimeHandler() != null) {
            return connection.getRealtimeHandler().getQueueSize();
        }
        return 0;
    }

    /**
     * Checks if the realtime message handler is running
     */
    public boolean isRealtimeHandlerRunning() {
        if (connection.getRealtimeHandler() != null) {
            return connection.getRealtimeHandler().isRunning();
        }
        return false;
    }
}
