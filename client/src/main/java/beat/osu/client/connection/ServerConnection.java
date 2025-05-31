package beat.osu.client.connection;

import beat.osu.shared.enums.MessageType;
import beat.osu.shared.models.Message;
import lombok.Setter;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerConnection {
    private static final String SERVER_HOST = "localhost";
    private static final Integer SERVER_PORT = 8081;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Thread readerThread;
    private boolean connected = false;

    private final BlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();
    private final Map<String, CompletableFuture<Object>> pendingRequests = new ConcurrentHashMap<>();

    @Setter
    private MessageCallback realtimeCallback;

    public interface MessageCallback {
        void onMessage(Object message);
    }

    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
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
                    handleServerMessage(receivedObject);
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

    private void handleServerMessage(Object message) {
        if (!pendingRequests.isEmpty()) {
            // Get the first pending request (FIFO) - since ConcurrentHashMap doesn't guarantee order
            String firstRequestId = pendingRequests.keySet().iterator().next();
            CompletableFuture<Object> future = pendingRequests.remove(firstRequestId);
            if (future != null) {
                future.complete(message);
                return;
            }
        }

        // If no pending requests or realtime callback is set, handle as realtime message
        if (realtimeCallback != null) {
            realtimeCallback.onMessage(message);
        } else {
            System.out.println("ServerConnection: Adding to message queue");
            messageQueue.offer(message);
        }
    }

    public CompletableFuture<Object> sendMessage(Message message) {
        CompletableFuture<Object> future = new CompletableFuture<>();

        try {
            pendingRequests.put(message.getId(), future);
            oos.writeObject(message);
            oos.flush();
        } catch (Exception e) {
            System.err.println("ServerConnection: Error sending message: " + e.getMessage());
            future.completeExceptionally(e);
            pendingRequests.remove(message.getId());
        }

        return future;
    }

    public void sendMessageAsync(Message message) {
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    public void disconnect() {
        connected = false;

        try {
            if (readerThread != null) {
                readerThread.interrupt();
            }
            if (oos != null) {
                Message disconnectMsg = new Message(MessageType.SYSTEM, "DISCONNECT",
                        null, System.currentTimeMillis());
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
