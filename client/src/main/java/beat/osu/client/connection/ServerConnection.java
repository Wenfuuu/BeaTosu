package beat.osu.client.connection;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

import beat.osu.client.config.ConfigurationManager;
import beat.osu.client.view.shared.common.Toast;
import beat.osu.shared.enums.message.MessageAction;
import beat.osu.shared.enums.message.MessageType;
import beat.osu.shared.models.RealtimeMessage;
import beat.osu.shared.models.RequestMessage;
import beat.osu.shared.models.ResponseMessage;
import javafx.application.Platform;
import lombok.Getter;

public class ServerConnection {
    private ConfigurationManager configurationManager;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Thread readerThread;
    private boolean connected = false;

    // Synchronization object for thread-safe writing to ObjectOutputStream
    private final Object writeLock = new Object();

    @Getter
    private RequestResponseHandler requestHandler;
    @Getter
    private RealtimeMessageHandler realtimeHandler;

    private boolean isReconnecting = false;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;

    public boolean connect() {
        try {
            configurationManager = ConfigurationManager.getInstance();
            String serverHost = configurationManager.getServerHost();
            int serverPort = configurationManager.getServerPort();

            socket = new Socket(serverHost, serverPort);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            requestHandler = new RequestResponseHandler(oos, writeLock);
            realtimeHandler = new RealtimeMessageHandler(oos, writeLock);

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
            } catch (EOFException e) {
                // Connection closed normally
                if (connected) {
                    System.out.println("Server closed connection");
                    Platform.runLater(() -> {
                        Toast.error("Server closed connection").show();
                    });
                }
            } catch (Exception e) {
                if (connected) {
                    System.err.println("Connection lost: " + e.getMessage());
                    e.printStackTrace(); // Print full stack trace for debugging

                    // Check if it's a stream corruption error and attempt reconnection
                    if (e instanceof java.io.StreamCorruptedException ||
                            e.getMessage().contains("invalid type code")) {
                        System.err.println("Stream corruption detected, attempting reconnection...");
                        disconnect();

                        // Attempt reconnection in a separate thread to avoid blocking
                        if (canReconnect()) {
                            new Thread(() -> {
                                boolean reconnected = reconnect();
                                Platform.runLater(() -> {
                                    if (reconnected) {
                                        Toast.success("Reconnected to server").show();
                                    } else {
                                        Toast.error("Failed to reconnect to server").show();
                                    }
                                });
                            }).start();
                        } else {
                            Platform.runLater(() -> {
                                Toast.error("Connection lost: " + e.getMessage()).show();
                            });
                        }
                    } else {
                        Platform.runLater(() -> {
                            Toast.error("Connection lost: " + e.getMessage()).show();
                        });
                        disconnect();
                    }
                }
            }
        });
        readerThread.setName("ServerConnection-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void routeIncomingMessage(Object message) {
        try {
            if (message instanceof ResponseMessage) {
                requestHandler.handleResponse((ResponseMessage) message);
            } else if (message instanceof RealtimeMessage) {
                realtimeHandler.handleIncomingMessage((RealtimeMessage) message);
            } else {
                System.err.println("Unknown message type received: " + message.getClass());
                System.err.println("Message content: " + message.toString());
            }
        } catch (Exception e) {
            System.err.println("Error routing message: " + e.getMessage());
            e.printStackTrace();

            // If we get stream corruption errors, disconnect and attempt reconnection
            if (e instanceof java.io.StreamCorruptedException ||
                    e.getMessage().contains("invalid type code")) {
                System.err.println("Stream corruption detected in message routing, disconnecting...");
                disconnect();

                // Attempt reconnection in a separate thread
                if (canReconnect()) {
                    new Thread(() -> {
                        boolean reconnected = reconnect();
                        Platform.runLater(() -> {
                            if (reconnected) {
                                Toast.success("Reconnected to server after message routing error").show();
                            } else {
                                Toast.error("Failed to reconnect after message routing error").show();
                            }
                        });
                    }).start();
                }
            }
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
                    synchronized (writeLock) {
                        RequestMessage disconnectMsg = new RequestMessage(
                                MessageType.SYSTEM, MessageAction.DISCONNECT, null);
                        oos.writeObject(disconnectMsg);
                        oos.flush();
                    }

                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println("Note: Could not send disconnect message: " + e.getMessage());
                }
            }

            if (oos != null)
                oos.close();
            if (ois != null)
                ois.close();
            if (socket != null)
                socket.close();

            System.out.println("Disconnected from server");

        } catch (Exception e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    public boolean reconnect() {
        if (isReconnecting) {
            return false;
        }

        isReconnecting = true;
        reconnectAttempts++;

        System.out.println(
                "Attempting to reconnect... (attempt " + reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS + ")");

        // Clean up current connection
        disconnect();

        // Wait a bit before reconnecting
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean success = connect();
        isReconnecting = false;

        if (success) {
            reconnectAttempts = 0;
            System.out.println("Reconnection successful");
        } else if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            System.err.println("Max reconnection attempts reached, giving up");
            reconnectAttempts = 0;
        }

        return success;
    }

    public boolean canReconnect() {
        return !isReconnecting && reconnectAttempts < MAX_RECONNECT_ATTEMPTS;
    }
}
