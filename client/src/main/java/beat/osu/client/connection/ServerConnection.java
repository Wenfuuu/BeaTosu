package beat.osu.client.connection;

import java.io.EOFException;
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
            
            // Optimize socket for low latency and high throughput
            socket.setTcpNoDelay(true); // Disable Nagle's algorithm for low latency
            socket.setSendBufferSize(64 * 1024); // 64KB send buffer
            socket.setReceiveBufferSize(64 * 1024); // 64KB receive buffer
            socket.setKeepAlive(true); // Enable keep-alive
            
            // Create ObjectOutputStream first and flush to establish stream header
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush(); // Important: Ensure stream header is sent
            
            // Small delay to ensure server has time to create its InputStream
            Thread.sleep(150);
            
            // Then create ObjectInputStream
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
            } catch (java.io.StreamCorruptedException e) {
                if (connected) {
                    // System.err.println("Stream corruption detected: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Specific handling for stream corruption
                    // System.err.println("Stream corruption detected, attempting reconnection...");
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
                            Toast.error("Stream corruption: " + e.getMessage()).show();
                        });
                    }
                }
            } catch (java.io.OptionalDataException e) {
                if (connected) {
                    // System.err.println("Data format error: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Handle optional data exception which can occur with stream corruption
                    // System.err.println("Data format error detected, attempting reconnection...");
                    disconnect();

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
                            Toast.error("Data format error: " + e.getMessage()).show();
                        });
                    }
                }
            } catch (Exception e) {
                if (connected) {
                    // System.err.println("Connection lost: " + e.getMessage());
                    e.printStackTrace(); // Print full stack trace for debugging

                    // Check if it's any kind of stream-related error
                    if (e.getMessage() != null && 
                        (e.getMessage().contains("invalid type code") ||
                         e.getMessage().contains("stream") ||
                         e.getMessage().contains("protocol"))) {
                        // System.err.println("Stream-related error detected, attempting reconnection...");
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
                // System.err.println("Unknown message type received: " + message.getClass());
                // System.err.println("Message content: " + message.toString());
            }
        } catch (Exception e) {
            // System.err.println("Error routing message: " + e.getMessage());
            e.printStackTrace();

            // If we get stream corruption errors, disconnect and attempt reconnection
            if (e instanceof java.io.StreamCorruptedException ||
                    e.getMessage().contains("invalid type code")) {
                // System.err.println("Stream corruption detected in message routing, disconnecting...");
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

        // Stop the realtime message handler first
        if (realtimeHandler != null) {
            realtimeHandler.stop();
        }

        try {
            // Interrupt the reader thread first
            if (readerThread != null && readerThread.isAlive()) {
                readerThread.interrupt();
                try {
                    readerThread.join(1000); // Wait up to 1 second for thread to finish
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Send disconnect message if possible
            if (oos != null && socket != null && !socket.isClosed()) {
                try {
                    synchronized (writeLock) {
                        RequestMessage disconnectMsg = new RequestMessage(
                                MessageType.SYSTEM, MessageAction.DISCONNECT, null);
                        oos.writeObject(disconnectMsg);
                        oos.flush();
                    }

                    Thread.sleep(100); // Give server time to process disconnect
                } catch (Exception e) {
                    System.out.println("Note: Could not send disconnect message: " + e.getMessage());
                }
            }

            // Close streams and socket
            if (oos != null) {
                try {
                    oos.close();
                } catch (Exception e) {
                    // System.err.println("Error closing ObjectOutputStream: " + e.getMessage());
                }
            }
            
            if (ois != null) {
                try {
                    ois.close();
                } catch (Exception e) {
                    // System.err.println("Error closing ObjectInputStream: " + e.getMessage());
                }
            }
            
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    // System.err.println("Error closing socket: " + e.getMessage());
                }
            }

            System.out.println("Disconnected from server");

        } catch (Exception e) {
            // System.err.println("Error during disconnect: " + e.getMessage());
        } finally {
            // Reset references
            oos = null;
            ois = null;
            socket = null;
            readerThread = null;
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

        // Clean up current connection thoroughly
        disconnect();

        // Wait longer before reconnecting to allow server cleanup
        try {
            Thread.sleep(2000); // Increased delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean success = connect();
        isReconnecting = false;

        if (success) {
            reconnectAttempts = 0;
            System.out.println("Reconnection successful");
        } else if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts = 0;
        }

        return success;
    }

    public boolean canReconnect() {
        return !isReconnecting && reconnectAttempts < MAX_RECONNECT_ATTEMPTS;
    }
}
