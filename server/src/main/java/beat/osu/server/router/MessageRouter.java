package beat.osu.server.router;

import beat.osu.server.service.AuthService;
import beat.osu.shared.dto.auth.requests.RegisterRequest;
import beat.osu.shared.models.Message;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MessageRouter {

    private final AuthService authService;

    public Object routeMessage(Message message, String clientId) {
        switch (message.getType()) {
            case USER:
                return handleAuthMessage(message, clientId);
            default:
                return Map.of("success", false, "message", "Unknown message type");
        }
    }

    private Object handleAuthMessage(Message message, String clientId) {
        switch (message.getAction()) {
            case "register":
                return authService.registerUser((RegisterRequest) message.getPayload(), clientId);
            default:
                return Map.of("success", false, "message", "Unknown authentication action");
        }
    }
}
