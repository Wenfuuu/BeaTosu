package beat.osu.server.handler;

import beat.osu.server.entities.User;
import beat.osu.server.router.MessageRouter;
import beat.osu.server.service.SessionService;
import beat.osu.server.service.UserService;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.dto.user.events.UserDisconnectedEvent;
import beat.osu.shared.enums.RealtimeMessageType;
import beat.osu.shared.models.RequestMessage;
import beat.osu.shared.models.RealtimeMessage;
import lombok.Getter;
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
    private final UserService userService;

    @Getter
    private final String clientId = UUID.randomUUID().toString();

    private static final ConcurrentHashMap<String, ClientHandler> activeClients = new ConcurrentHashMap<>();
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    
    private RequestResponseHandler requestResponseHandler;
    private RealtimeMessageHandler realtimeMessageHandler;

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());

            requestResponseHandler = new RequestResponseHandler(messageRouter, oos);
            realtimeMessageHandler = new RealtimeMessageHandler(oos, clientId);

            activeClients.put(clientId, this);
            sessionService.createSessionData(clientId);

            System.out.println("Client connected from address: " + clientSocket.getInetAddress() + ": " + clientSocket.getPort() + " with client ID: " + clientId);

            Object receivedObject;
            while ((receivedObject = ois.readObject()) != null) {
                routeIncomingMessage(receivedObject);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            cleanup();
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

    private void routeIncomingMessage(Object message) {
        try {
            if (message instanceof RequestMessage) {
                requestResponseHandler.handleRequest((RequestMessage) message, clientId);
            } else if (message instanceof RealtimeMessage) {
                realtimeMessageHandler.handleRealtimeMessage((RealtimeMessage) message, clientId);
            }  else {
                sendError("Invalid message format received: " + message.getClass().getSimpleName());
            }
        } catch (Exception e) {
            System.err.println("ClientHandler: Error routing message: " + e.getMessage());
            sendError("Error processing message: " + e.getMessage());
        }
    }

    private void cleanup() {
        try {
            Object userId = sessionService.getSessionData(clientId, "userId");
            UserDto disconnectedUser = null;

            if (userId != null) {
                User user = userService.findUserById((Integer) userId);
                if (user != null) {
                    disconnectedUser = new UserDto(
                            user.getId(), user.getUsername(), user.getEmail(), user.getCountryCode(),
                            user.getProfilePicture(), user.getPerformance(), user.getAccuracy(),
                            user.getPlayCount(), user.getLevel(), userService.getUserRank(user.getId()), user.isSupporter()
                    );
                }
            }

            activeClients.remove(clientId);
            sessionService.removeSession(clientId);
            
            if (realtimeMessageHandler != null) {
                realtimeMessageHandler.cleanup();
            }

            if (disconnectedUser != null) {
                UserDisconnectedEvent event = new UserDisconnectedEvent(disconnectedUser);
                RealtimeMessage userDisconnectedMessage = new RealtimeMessage(
                        RealtimeMessageType.USER_DISCONNECTED,
                        "SYSTEM",
                        event
                );
                RealtimeMessageHandler.broadcastToAll(userDisconnectedMessage);
            }
            
            if (oos != null) oos.close();
            if (ois != null) ois.close();
            if (clientSocket != null) clientSocket.close();
            System.out.println("Client disconnected: " + clientId);
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    public void sendRealtimeMessage(RealtimeMessage message) {
        if (realtimeMessageHandler != null) {
            realtimeMessageHandler.sendToClient(message, clientId);
        }
    }
}
