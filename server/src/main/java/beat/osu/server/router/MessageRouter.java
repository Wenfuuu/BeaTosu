package beat.osu.server.router;

import beat.osu.server.service.AuthService;
import beat.osu.server.service.BeatmapService;
import beat.osu.server.service.ChannelService;
import beat.osu.server.service.MatchService;
import beat.osu.server.service.PrivateChatService;
import beat.osu.server.service.ScoreService;
import beat.osu.server.service.SessionService;
import beat.osu.server.service.SpectateService;
import beat.osu.server.service.SystemService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.auth.requests.LoginRequest;
import beat.osu.shared.dto.auth.requests.RegisterRequest;
import beat.osu.shared.dto.beatmap.requests.GetBeatmapByIdRequest;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapRequest;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapSetRequest;
import beat.osu.shared.dto.chat.requests.JoinChannelRequest;
import beat.osu.shared.dto.chat.requests.LeaveChannelRequest;
import beat.osu.shared.dto.chat.requests.SendChannelMessageRequest;
import beat.osu.shared.dto.chat.requests.SendPrivateChatMessageRequest;
import beat.osu.shared.dto.game.requests.NotifySpectateStatusRequest;
import beat.osu.shared.dto.game.requests.SendSpectateEventRequest;
import beat.osu.shared.dto.game.requests.StartSpectateRequest;
import beat.osu.shared.dto.match.requests.ChangeMatchSlotRequest;
import beat.osu.shared.dto.match.requests.CreateMatchRequest;
import beat.osu.shared.dto.match.requests.JoinMatchRequest;
import beat.osu.shared.dto.match.requests.KickPlayerRequest;
import beat.osu.shared.dto.match.requests.LeaveMatchRequest;
import beat.osu.shared.dto.match.requests.PlayerFinishedEventRequest;
import beat.osu.shared.dto.match.requests.SendMatchScoreEventRequest;
import beat.osu.shared.dto.match.requests.StartMatchRequest;
import beat.osu.shared.dto.match.requests.TransferHostRequest;
import beat.osu.shared.dto.match.requests.UpdateMatchBeatmapRequest;
import beat.osu.shared.dto.match.requests.UpdateMatchChangingBeatmapRequest;
import beat.osu.shared.dto.match.requests.UpdateMatchNameRequest;
import beat.osu.shared.dto.match.requests.UpdateMatchPasswordRequest;
import beat.osu.shared.dto.match.requests.UpdateMatchWinConditionRequest;
import beat.osu.shared.dto.match.requests.UpdatePlayerStatusRequest;
import beat.osu.shared.dto.score.requests.GetScoreRequest;
import beat.osu.shared.dto.score.requests.InsertScoreRequest;
import beat.osu.shared.dto.session.requests.CreateSessionDataRequest;
import beat.osu.shared.dto.session.requests.GetSessionDataRequest;
import beat.osu.shared.dto.session.requests.RemoveSessionDataRequest;
import beat.osu.shared.models.RequestMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageRouter {

    private final SystemService systemService;
    private final AuthService authService;
    private final BeatmapService beatmapService;
    private final ScoreService scoreService;
    private final ChannelService channelService;
    private final PrivateChatService privateChatService;
    private final MatchService matchService;
    private final SessionService sessionService;
    private final SpectateService spectateService;

    public void cleanupDisconnectedUser(int userId) {
        try {
            System.out.println("Cleaning up disconnected user: " + userId);
            
            matchService.removeUserFromAllMatches(userId);
            channelService.removeUserFromAllChannels(userId);
            spectateService.removeUserFromAllSpectating(userId);
            
            System.out.println("Successfully cleaned up disconnected user: " + userId);
        } catch (Exception e) {
            System.err.println("Error cleaning up disconnected user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Object routeRequestMessage(RequestMessage request, String clientId) {
        switch (request.getType()) {
            case SYSTEM:
                return handleSystemRequest(request, clientId);
            case AUTH:
                return handleAuthRequest(request, clientId);
            case BEATMAP:
                return handleBeatmapRequest(request, clientId);
            case SCORE:
                return handleScoreRequest(request, clientId);
            case CHANNEL:
                return handleChannelRequest(request, clientId);
            case PRIVATE_CHAT:
                return handlePrivateChatRequest(request, clientId);
            case MATCH:
                return handleMatchRequest(request, clientId);
            case SESSION:
                return handleSessionRequest(request, clientId);
            case SPECTATE:
                return handleSpectateRequest(request, clientId);
            default:
                return Result.failure(Error.validation("Unknown request type: " + request.getType()));
        }
    }

    private Object handleSystemRequest(RequestMessage request, String clientId) {
        switch (request.getAction()) {
            case GET_CONNECTED_USERS:
                return systemService.getConnectedUsers();
            case DISCONNECT:
                return Result.success("Disconnection acknowledged");
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
            case GET_BEATMAP_BY_ID:
                return beatmapService.getBeatmapById((GetBeatmapByIdRequest) request.getPayload());
            case INSERT_BEATMAP:
                return beatmapService.insertBeatmap((InsertBeatmapRequest) request.getPayload());
            case INSERT_BEATMAP_SET:
                return beatmapService.insertBeatmapSet((InsertBeatmapSetRequest) request.getPayload());
            default:
                return Result.failure(Error.validation("Unknown beatmap action: " + request.getAction()));
        }
    }

    private Object handleScoreRequest(RequestMessage request, String clientId) {
        switch (request.getAction()) {
            case GET_ALL_SCORES:
                return scoreService.getScoresByBeatmapId((GetScoreRequest) request.getPayload());
            case INSERT_SCORE:
                return scoreService.insertScore((InsertScoreRequest) request.getPayload());
            default:
                return Result.failure(Error.validation("Unknown score action: " + request.getAction()));
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
            case TRANSFER_HOST:
                return matchService.transferHost((TransferHostRequest) request.getPayload(), clientId);
            case START_MATCH:
                return matchService.startMatch((StartMatchRequest) request.getPayload(), clientId);
            case SEND_MATCH_SCORE_EVENT:
                return matchService.sendMatchScoreEvent((SendMatchScoreEventRequest) request.getPayload(), clientId);
            case CHANGE_MATCH_SLOT:
                return matchService.changeMatchSlot((ChangeMatchSlotRequest) request.getPayload(), clientId);
            case UPDATE_MATCH_PASSWORD:
                return matchService.updateMatchPassword((UpdateMatchPasswordRequest) request.getPayload(), clientId);
            case UPDATE_MATCH_NAME:
                return matchService.updateMatchName((UpdateMatchNameRequest) request.getPayload(), clientId);
            case UPDATE_MATCH_BEATMAP:
                return matchService.updateMatchBeatmap((UpdateMatchBeatmapRequest) request.getPayload(), clientId);
            case UPDATE_MATCH_CHANGING_BEATMAP:
                return matchService.updateMatchChangingBeatmap((UpdateMatchChangingBeatmapRequest) request.getPayload(), clientId);
            case UPDATE_MATCH_WIN_CONDITION:
                return matchService.updateMatchWinCondition((UpdateMatchWinConditionRequest) request.getPayload(), clientId);
            case UPDATE_PLAYER_STATUS:
                return matchService.updatePlayerStatus((UpdatePlayerStatusRequest) request.getPayload(), clientId);
            case PLAYER_FINISHED_MATCH:
                return matchService.playerFinishedMatch((PlayerFinishedEventRequest) request.getPayload(), clientId);
            default:
                return Result.failure(Error.validation("Unknown match action: " + request.getAction()));
        }
    }

    private Object handleSessionRequest(RequestMessage request, String clientId) {
        switch (request.getAction()) {
            case CREATE_SESSION_DATA:
                return sessionService.createSessionData((CreateSessionDataRequest) request.getPayload(), clientId);
            case REMOVE_SESSION_DATA:
                return sessionService.removeSessionData((RemoveSessionDataRequest) request.getPayload(), clientId);
            case GET_SESSION_DATA:
                return sessionService.getSessionData((GetSessionDataRequest) request.getPayload(), clientId);
            default:
                return Result.failure(Error.validation("Unknown session action: " + request.getAction()));
        }
    }

    private Object handleSpectateRequest(RequestMessage request, String clientId) {
        switch (request.getAction()) {
            case START_SPECTATE:
                return spectateService.startSpectate((StartSpectateRequest) request.getPayload(), clientId);
            case SEND_SPECTATE_EVENT:
                return spectateService.sendSpectateEvent((SendSpectateEventRequest) request.getPayload(), clientId);
            case STOP_SPECTATE:
                return spectateService.stopSpectating(clientId);
            case CHANGE_SPECTATE_STATUS:
                return spectateService.notifySpectatorsStatusChange((NotifySpectateStatusRequest) request.getPayload(), clientId);
            case PLAYER_EXIT_GAME:
                return spectateService.notifySpectatorsPlayerExited(clientId);
            default:
                return Result.failure(Error.validation("Unknown spectate action: " + request.getAction()));
        }
    }
}