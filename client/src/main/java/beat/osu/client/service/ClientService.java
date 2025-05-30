package beat.osu.client.service;

import beat.osu.client.connection.ServerConnection;

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
}
