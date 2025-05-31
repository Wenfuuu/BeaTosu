package beat.osu.client.service;

import beat.osu.client.connection.ServerConnection;
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

    public void setRealtimeMessageHandler(ServerConnection.MessageCallback callback) {
        connection.setRealtimeCallback(callback);
    }

    public boolean isConnected() {
        return connection.isConnected();
    }
}
