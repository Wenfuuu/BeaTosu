package beat.osu.server.handler;

import beat.osu.server.router.MessageRouter;
import beat.osu.server.service.SessionService;
import beat.osu.shared.enums.MessageAction;
import beat.osu.shared.models.Message;
import lombok.RequiredArgsConstructor;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final MessageRouter messageRouter;
    private final SessionService sessionService;
    private final String clientId = UUID.randomUUID().toString();

    private static final ConcurrentHashMap<String, ClientHandler> activeClients = new ConcurrentHashMap<>();
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());

            activeClients.put(clientId, this);
            sessionService.createSession(clientId);

            System.out.println("Client connected from address: " + clientSocket.getInetAddress() + ": " + clientSocket.getPort() + " with client ID: " + clientId);

            Object receivedObject;
            while ((receivedObject = ois.readObject()) != null) {
                if (receivedObject instanceof Message) {
                    handleMessage((Message) receivedObject);
                } else {
                    sendError("Invalid message format received.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            cleanup();
        }
    }

    private void handleMessage(Message message) {
        try {
            if (message.getAction() == MessageAction.DISCONNECT) {
                sendResponse(Map.of("status", "goodbye"));
                return;
            }

            Object response = messageRouter.routeMessage(message, clientId);
            sendResponse(response);

        } catch (Exception e) {
            sendError("Error processing message: " + e.getMessage());
        }
    }

    private void sendResponse(Object response) {
        try {
            oos.writeObject(response);
            oos.flush();
        } catch (Exception e) {
            System.err.println("Error sending response to client: " + e.getMessage());
        }
    }

    private void sendError(String error) {
        sendResponse(Map.of("error", error));
    }

    private void cleanup() {
        try {
            activeClients.remove(clientId);
            sessionService.removeSession(clientId);
            if (oos != null) oos.close();
            if (ois != null) ois.close();
            if (clientSocket != null) clientSocket.close();
            System.out.println("Client disconnected: " + clientId);
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}
