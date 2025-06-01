package beat.osu.server.router;

import beat.osu.server.service.AuthService;
import beat.osu.server.service.BeatmapService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.auth.requests.LoginRequest;
import beat.osu.shared.dto.auth.requests.RegisterRequest;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapRequest;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapSetRequest;
import beat.osu.shared.models.Message;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageRouter {

    private final AuthService authService;
    private final BeatmapService beatmapService;

    public Object routeMessage(Message message, String clientId) {
        switch (message.getType()) {
            case AUTH:
                return handleAuthMessage(message, clientId);
            case BEATMAP:
                return handleBeatmapMessage(message, clientId);
            default:
                return Result.failure(Error.validation("Unknown message type: " + message.getType()));
        }
    }

    private Object handleAuthMessage(Message message, String clientId) {
        switch (message.getAction()) {
            case REGISTER:
                return authService.registerUser((RegisterRequest) message.getPayload(), clientId);
            case LOGIN:
                return authService.loginUser((LoginRequest) message.getPayload(), clientId);
            default:
                return Result.failure(Error.validation("Unknown authentication action: " + message.getAction()));
        }
    }

    private Object handleBeatmapMessage(Message message, String clientId) {
        switch (message.getAction()) {
            case GET_ALL_BEATMAPS:
                return beatmapService.getAllBeatmaps();
            case INSERT_BEATMAP:
                return beatmapService.insertBeatmap((InsertBeatmapRequest) message.getPayload());
            case INSERT_BEATMAP_SET:
                return beatmapService.insertBeatmapSet((InsertBeatmapSetRequest) message.getPayload());
            default:
                return Result.failure(Error.validation("Unknown beatmap action: " + message.getAction()));
        }
    }
}
