package beat.osu.client.connection;

import beat.osu.shared.enums.MessageAction;
import beat.osu.shared.enums.MessageType;
import beat.osu.shared.models.RealtimeMessage;
import beat.osu.shared.models.RequestMessage;
import beat.osu.shared.models.ResponseMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ServerConnection {
    private static final String SERVER_HOST = "localhost";
    private static final Integer SERVER_PORT = 8081;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Thread readerThread;
    private boolean connected = false;

    private RequestResponseHandler requestHandler;
    private RealtimeMessageHandler realtimeHandler;

    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            requestHandler = new RequestResponseHandler(oos);
            realtimeHandler = new RealtimeMessageHandler(oos);

            connected = true;
            startReaderThread();

            System.out.println("Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
            if (oos != null) {
                RequestMessage disconnectMsg = new RequestMessage(
                        MessageType.SYSTEM, MessageAction.DISCONNECT, null);
                oos.writeObject(disconnectMsg);
                oos.flush();
                oos.close();
            }
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
