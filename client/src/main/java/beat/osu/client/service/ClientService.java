package beat.osu.client.service;

import beat.osu.client.connection.ServerConnection;
import lombok.Getter;

@Getter
public class ClientService {
    private final ServerConnection connection;

    public ClientService() {
        this.connection = new ServerConnection();
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
