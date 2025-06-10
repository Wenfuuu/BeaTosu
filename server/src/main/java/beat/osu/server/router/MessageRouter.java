package beat.osu.server.router;

import beat.osu.server.service.*;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.auth.requests.LoginRequest;
import beat.osu.shared.dto.auth.requests.RegisterRequest;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapRequest;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapSetRequest;
import beat.osu.shared.dto.chat.requests.*;
import beat.osu.shared.models.RequestMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageRouter {

    private final SystemService systemService;
    private final AuthService authService;
    private final BeatmapService beatmapService;
    private final ChannelService channelService;
    private final PrivateChatService privateChatService;

    public Object routeRequestMessage(RequestMessage request, String clientId) {
        switch (request.getType()) {
            case SYSTEM:
                return handleSystemRequest(request, clientId);
            case AUTH:
                return handleAuthRequest(request, clientId);
            case BEATMAP:
                return handleBeatmapRequest(request, clientId);
            case CHANNEL:
                return handleChannelRequest(request, clientId);
            case PRIVATE_CHAT:
                return handlePrivateChatRequest(request, clientId);
            default:
                return Result.failure(Error.validation("Unknown request type: " + request.getType()));
        }
    }

    private Object handleSystemRequest(RequestMessage request, String clientId) {
        switch (request.getAction()) {
            case GET_CONNECTED_USERS:
                return systemService.getConnectedUsers();
            default:
                return Result.failure(Error.validation("Unknown system action: " + request.getAction()));
        }
    }

    private Object handleAuthRequest(RequestMessage request, String clientId) {
        switch (request.getAction()) {
            case REGISTER:
                return authService.registerUser((RegisterRequest) request.getPayload(), clientId);
            case LOGIN:
                return authService.loginUser((LoginRequest) request.getPayload(), clientId);
            default:
                return Result.failure(Error.validation("Unknown authentication action: " + request.getAction()));
        }
    }

    private Object handleBeatmapRequest(RequestMessage request, String clientId) {
        switch (request.getAction()) {
            case GET_ALL_BEATMAPS:
                return beatmapService.getAllBeatmaps();
            case INSERT_BEATMAP:
                return beatmapService.insertBeatmap((InsertBeatmapRequest) request.getPayload());
            case INSERT_BEATMAP_SET:
                return beatmapService.insertBeatmapSet((InsertBeatmapSetRequest) request.getPayload());
            default:
                return Result.failure(Error.validation("Unknown beatmap action: " + request.getAction()));
        }
    }

    private Object handleChannelRequest(RequestMessage request, String clientId) {
        switch (request.getAction()) {
            case GET_ALL_CHANNELS:
                return channelService.getAllChannels(clientId);
            case GET_JOINED_CHANNELS:
                return channelService.getJoinedChannels(clientId);
            case JOIN_CHANNEL:
                return channelService.joinChannel((JoinChannelRequest) request.getPayload(), clientId);
            case LEAVE_CHANNEL:
                return channelService.leaveChannel((LeaveChannelRequest) request.getPayload(), clientId);
            case SEND_CHANNEL_MESSAGE:
                return channelService.sendChannelMessage((SendChannelMessageRequest) request.getPayload(), clientId);
            default:
                return Result.failure(Error.validation("Unknown channel action: " + request.getAction()));
        }
    }

    private Object handlePrivateChatRequest(RequestMessage request, String clientId) {
        switch (request.getAction()) {
            case SEND_PRIVATE_CHAT_MESSAGE:
                return privateChatService.sendPrivateMessage((SendPrivateChatMessageRequest) request.getPayload(), clientId);
            default:
                return Result.failure(Error.validation("Unknown private chat action: " + request.getAction()));
        }
    }
}