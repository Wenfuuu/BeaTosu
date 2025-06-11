package beat.osu.server.router;

import beat.osu.server.service.AuthService;
import beat.osu.server.service.BeatmapService;
import beat.osu.server.service.ChannelService;
import beat.osu.server.service.MatchService;
import beat.osu.server.service.PrivateChatService;
import beat.osu.server.service.SystemService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.auth.requests.LoginRequest;
import beat.osu.shared.dto.auth.requests.RegisterRequest;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapRequest;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapSetRequest;
import beat.osu.shared.dto.chat.requests.JoinChannelRequest;
import beat.osu.shared.dto.chat.requests.LeaveChannelRequest;
import beat.osu.shared.dto.chat.requests.SendChannelMessageRequest;
import beat.osu.shared.dto.chat.requests.SendPrivateChatMessageRequest;
import beat.osu.shared.dto.match.requests.CreateMatchRequest;
import beat.osu.shared.dto.match.requests.JoinMatchRequest;
import beat.osu.shared.dto.match.requests.KickPlayerRequest;
import beat.osu.shared.dto.match.requests.LeaveMatchRequest;
import beat.osu.shared.models.RequestMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageRouter {

    private final SystemService systemService;
    private final AuthService authService;
    private final BeatmapService beatmapService;
    private final ChannelService channelService;
    private final PrivateChatService privateChatService;
    private final MatchService matchService;

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
            case MATCH:
                return handleMatchRequest(request, clientId);
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

    private Object handleMatchRequest(RequestMessage request, String clientId) {
        switch (request.getAction()) {
            case GET_ALL_MATCHES:
                return matchService.getAllMatches(clientId);
            case CREATE_MATCH:
                return matchService.createMatch((CreateMatchRequest) request.getPayload(), clientId);
            case JOIN_MATCH:
                return matchService.joinMatch((JoinMatchRequest) request.getPayload(), clientId);
            case LEAVE_MATCH:
                return matchService.leaveMatch((LeaveMatchRequest) request.getPayload(), clientId);
            case KICK_PLAYER:
                return matchService.kickPlayer((KickPlayerRequest) request.getPayload(), clientId);
            default:
                return Result.failure(Error.validation("Unknown match action: " + request.getAction()));
        }
    }
}