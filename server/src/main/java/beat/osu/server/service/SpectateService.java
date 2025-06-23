package beat.osu.server.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import beat.osu.server.entities.User;
import beat.osu.server.handler.RealtimeMessageHandler;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.game.SpectateDto;
import beat.osu.shared.dto.game.events.SpectateEvent;
import beat.osu.shared.dto.game.requests.SendSpectateEventRequest;
import beat.osu.shared.dto.game.requests.StartSpectateRequest;
import beat.osu.shared.dto.game.responses.SendSpectateEventResponse;
import beat.osu.shared.dto.game.responses.StartSpectateResponse;
import beat.osu.shared.dto.game.responses.StopSpectateResponse;
import beat.osu.shared.enums.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;

public class SpectateService {

    private final SessionService sessionService;
    private final UserService userService;

    // Map to track who is spectating whom: spectatorUserId -> playingUserId
    private final Map<Integer, Integer> spectatorToPlayer = new ConcurrentHashMap<>();
    // Map to track who is being spectated by whom: playingUserId ->
    // Set<spectatorUserIds>
    private final Map<Integer, Set<Integer>> playerToSpectators = new ConcurrentHashMap<>();

    public SpectateService(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    public Result<StartSpectateResponse> startSpectate(StartSpectateRequest request, String clientId) {
        Integer spectatorUserId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (spectatorUserId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        SpectateDto spectateDto = request.getSpectateDto();
        int playingUserId = spectateDto.getPlayingUserId();

        if (spectatorUserId == playingUserId) {
            return Result.failure(Error.validation("Cannot spectate yourself"));
        }

        User spectator = userService.findUserById(spectatorUserId);
        User player = userService.findUserById(playingUserId);

        if (spectator == null || player == null) {
            return Result.failure(Error.notFound("User not found"));
        }

        // Stop spectating current player if any
        stopSpectate(spectatorUserId);

        // Add spectator to player's spectator list
        spectatorToPlayer.put(spectatorUserId, playingUserId);
        playerToSpectators.computeIfAbsent(playingUserId, k -> ConcurrentHashMap.newKeySet()).add(spectatorUserId);

        System.out.println("Started spectating: " + player.getUsername() + " by spectator: " + spectator.getUsername());
        return Result.success(new StartSpectateResponse("Started spectating " + player.getUsername()));
    }

    public Result<SendSpectateEventResponse> sendSpectateEvent(SendSpectateEventRequest request, String clientId) {
        Integer playingUserId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (playingUserId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        SpectateEvent spectateEvent = request.getSpectateEvent();

        Set<Integer> spectators = playerToSpectators.get(playingUserId);
        if (spectators != null && !spectators.isEmpty()) {
            RealtimeMessage realtimeMessage = new RealtimeMessage(
                    RealtimeMessageType.SPECTATE_EVENT,
                    clientId,
                    spectateEvent);

            // Send to all spectators
            for (Integer spectatorUserId : spectators) {
                String spectatorClientId = sessionService.getClientIdByUserId(spectatorUserId);
                if (spectatorClientId != null) {
                    System.out.println("Sending spectate event to spectator: " + spectatorUserId);
                    RealtimeMessageHandler.sendToClient(realtimeMessage, spectatorClientId);
                }
            }
        }

        return Result.success(new SendSpectateEventResponse("Spectate event sent to " +
                (spectators != null ? spectators.size() : 0) + " spectators"));
    }

    public Result<StopSpectateResponse> stopSpectating(String clientId) {
        Integer spectatorUserId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (spectatorUserId != null) {
            stopSpectate(spectatorUserId);
        }

        System.out.println("Stopped spectating for user: " + spectatorUserId);
        return Result.success(new StopSpectateResponse("Stopped spectating for user: " + spectatorUserId));
    }

    public void stopSpectate(int spectatorUserId) {
        Integer currentPlayingUserId = spectatorToPlayer.remove(spectatorUserId);
        if (currentPlayingUserId != null) {
            Set<Integer> spectators = playerToSpectators.get(currentPlayingUserId);
            if (spectators != null) {
                spectators.remove(spectatorUserId);
                if (spectators.isEmpty()) {
                    playerToSpectators.remove(currentPlayingUserId);
                }
            }
        }
    }

    public void removeUserFromAllSpectating(int userId) {
        // Stop spectating if user is a spectator
        stopSpectate(userId);

        // Remove user as a player being spectated
        Set<Integer> spectators = playerToSpectators.remove(userId);
        if (spectators != null) {
            // Remove this user from all spectators' maps
            for (Integer spectatorUserId : spectators) {
                spectatorToPlayer.remove(spectatorUserId);
            }
        }
    }

    public Set<Integer> getSpectators(int playingUserId) {
        Set<Integer> spectators = playerToSpectators.get(playingUserId);
        return spectators != null ? Set.copyOf(spectators) : Set.of();
    }

    public Integer getSpectatingUser(int spectatorUserId) {
        return spectatorToPlayer.get(spectatorUserId);
    }
}
