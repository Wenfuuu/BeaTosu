package beat.osu.client.connection;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

import beat.osu.client.config.ConfigurationManager;
import beat.osu.shared.enums.message.MessageAction;
import beat.osu.shared.enums.message.MessageType;
import beat.osu.shared.models.RealtimeMessage;
import beat.osu.shared.models.RequestMessage;
import beat.osu.shared.models.ResponseMessage;
import lombok.Getter;

public class ServerConnection {
    private ConfigurationManager configurationManager;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Thread readerThread;
    private boolean connected = false;

    @Getter
    private RequestResponseHandler requestHandler;
    @Getter
    private RealtimeMessageHandler realtimeHandler;

    public boolean connect() {
        try {
            configurationManager = ConfigurationManager.getInstance();
            String serverHost = configurationManager.getServerHost();
            int serverPort = configurationManager.getServerPort();

            socket = new Socket(serverHost, serverPort);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            requestHandler = new RequestResponseHandler(oos);
            realtimeHandler = new RealtimeMessageHandler(oos);

            connected = true;
            startReaderThread();

            System.out.println("Connected to server at " + serverHost + ":" + serverPort);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }

    private void startReaderThread() {
        readerThread = new Thread(() -> {
            try {
                Object receivedObject;
                while (connected && (receivedObject = ois.readObject()) != null) {
                    routeIncomingMessage(receivedObject);
                }
            } catch (Exception e) {
                if (connected) {
                    System.out.println("Connection lost: " + e.getMessage());
                    disconnect();
                }
            }
        });
        readerThread.start();
    }

    private void routeIncomingMessage(Object message) {
        if (message instanceof ResponseMessage) {
            requestHandler.handleResponse((ResponseMessage) message);
        } else if (message instanceof RealtimeMessage) {
            realtimeHandler.handleIncomingMessage((RealtimeMessage) message);
        } else {
            System.err.println("Unknown message type received: " + message.getClass());
        }
    }

    public CompletableFuture<Object> sendRequest(RequestMessage request) {
        return requestHandler.sendRequest(request);
    }

    public void addRealtimeMessageCallback(RealtimeMessageHandler.RealtimeMessageCallback callback) {
        realtimeHandler.addCallback(callback);
    }

    public void removeRealtimeMessageCallback(RealtimeMessageHandler.RealtimeMessageCallback callback) {
        realtimeHandler.removeCallback(callback);
    }

    public void sendRealtimeMessage(RealtimeMessage message) {
        realtimeHandler.sendRealtimeMessage(message);
    }

    public void disconnect() {
        connected = false;

        try {
            if (readerThread != null) {
                readerThread.interrupt();
            }
            
            if (oos != null && socket != null && !socket.isClosed()) {
                try {
                    RequestMessage disconnectMsg = new RequestMessage(
                            MessageType.SYSTEM, MessageAction.DISCONNECT, null);
                    oos.writeObject(disconnectMsg);
                    oos.flush();
                    
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println("Note: Could not send disconnect message: " + e.getMessage());
                }
            }
            
            if (oos != null) oos.close();
            if (ois != null) ois.close();
            if (socket != null) socket.close();

            System.out.println("Disconnected from server");

        } catch (Exception e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}
