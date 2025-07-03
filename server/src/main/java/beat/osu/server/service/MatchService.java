package beat.osu.server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import beat.osu.server.entities.Match;
import beat.osu.server.entities.MatchPlayer;
import beat.osu.server.entities.User;
import beat.osu.server.handler.RealtimeMessageHandler;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.BeatmapDto;
import beat.osu.shared.dto.beatmap.requests.GetBeatmapByIdRequest;
import beat.osu.shared.dto.beatmap.responses.GetBeatmapByIdResponse;
import beat.osu.shared.dto.match.MatchDto;
import beat.osu.shared.dto.match.MatchPlayerDto;
import beat.osu.shared.dto.match.events.*;
import beat.osu.shared.dto.match.requests.*;
import beat.osu.shared.dto.match.responses.*;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.enums.match.MatchWinCondition;
import beat.osu.shared.enums.match.PlayerRole;
import beat.osu.shared.enums.match.PlayerStatus;
import beat.osu.shared.enums.message.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;

public class MatchService {

    private final Map<Integer, Match> matches = new ConcurrentHashMap<>();
    private final Map<Integer, Set<MatchPlayer>> matchPlayers = new ConcurrentHashMap<>(); // matchId -> players
    private final Map<Integer, Integer> userToMatch = new ConcurrentHashMap<>(); // userId -> matchId (since user can only be in one match)

    private final SessionService sessionService;
    private final UserService userService;
    private final BeatmapService beatmapService;

    private final AtomicInteger matchIdGenerator = new AtomicInteger(1);
    private final AtomicInteger matchPlayerIdGenerator = new AtomicInteger(1);

    public MatchService(SessionService sessionService, UserService userService, BeatmapService beatmapService) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.beatmapService = beatmapService;
    }

    private boolean isUserInMatch(int matchId, int userId) {
        Integer userCurrentMatch = userToMatch.get(userId);
        return userCurrentMatch != null && userCurrentMatch == matchId;
    }

    private boolean isUserInAnyMatch(int userId) {
        return userToMatch.containsKey(userId);
    }

    private MatchPlayer findPlayerInMatch(int matchId, int userId) {
        Set<MatchPlayer> players = matchPlayers.get(matchId);
        if (players == null) return null;
        return players.stream()
                .filter(player -> player.getUserId() == userId)
                .findFirst()
                .orElse(null);
    }

    private void removePlayerFromMatch(int matchId, int userId) {
        Set<MatchPlayer> players = matchPlayers.get(matchId);
        
        if (players != null) {
            players.removeIf(player -> player.getUserId() == userId);
        }
        
        userToMatch.remove(userId);
    }

    public Result<GetAllMatchesResponse> getAllMatches(String clientId) {
        List<MatchDto> matchDtos = new ArrayList<>();

        List<Match> matchList = new ArrayList<>(matches.values());
        for (Match match : matchList) {
            MatchDto matchDto = convertToMatchDto(match);
            matchDtos.add(matchDto);
        }

        return Result.success(new GetAllMatchesResponse(matchDtos));
    }

    public Result<CreateMatchResponse> createMatch(CreateMatchRequest request, String clientId) {
        Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return Result.failure(Error.validation("Match name cannot be empty"));
        }

        if (request.getMaxPlayerCount() < 2 || request.getMaxPlayerCount() > 16) {
            return Result.failure(Error.validation("Max player count must be between 2 and 16"));
        }

        if (isUserInAnyMatch(userId)) {
            return Result.failure(Error.validation("You are already in a match"));
        }

        int matchId = matchIdGenerator.getAndIncrement();
        Match match = new Match(
                matchId,
                request.getName().trim(),
                request.getPassword(),
                false,
                false,
                request.getMaxPlayerCount(),
                request.getBeatmapId(),
                MatchWinCondition.SCORE
        );

        matches.put(matchId, match);
        matchPlayers.put(matchId, ConcurrentHashMap.newKeySet());

        int hostPlayerId = matchPlayerIdGenerator.getAndIncrement();
        MatchPlayer hostPlayer = new MatchPlayer(hostPlayerId, matchId, userId, PlayerRole.HOST, PlayerStatus.NOT_READY, 0);
        matchPlayers.get(matchId).add(hostPlayer);
        userToMatch.put(userId, matchId);

        MatchDto matchDto = convertToMatchDto(match);
        String message = "Match created successfully: " + match.getName();
        Result<CreateMatchResponse> response = Result.success(new CreateMatchResponse(matchDto, message));

        if (response.isSuccess()) {
            MatchCreatedEvent event = new MatchCreatedEvent(matchDto);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.MATCH_CREATED, clientId, event);
            RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        }

        return response;
    }

    public Result<JoinMatchResponse> joinMatch(JoinMatchRequest request, String clientId) {
        int matchId = request.getMatchId();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        if (isUserInAnyMatch(userId)) {
            return Result.failure(Error.validation("You are already in a match"));
        }

        if (isUserInMatch(matchId, userId)) {
            return Result.failure(Error.validation("You are already in this match"));
        }

        if (matchPlayers.get(matchId).size() >= match.getMaxPlayerCount()) {
            return Result.failure(Error.validation("Match is full"));
        }

        if (match.getPassword() != null && !match.getPassword().isEmpty()) {
            if (request.getPassword() == null || !request.getPassword().equals(match.getPassword())) {
                return Result.failure(Error.validation("Invalid password"));
            }
        }

        if (isUserInMatch(matchId, userId)) {
            return Result.failure(Error.validation("You are already in this match"));
        }

        int availableSlot = findAvailableSlot(matchId);
        if (availableSlot == -1) {
            return Result.failure(Error.validation("No available slots"));
        }

        int newPlayerId = matchPlayerIdGenerator.getAndIncrement();
        MatchPlayer newPlayer = new MatchPlayer(newPlayerId, matchId, userId, PlayerRole.PLAYER, PlayerStatus.NOT_READY, availableSlot);
        matchPlayers.get(matchId).add(newPlayer);
        userToMatch.put(userId, matchId);

        MatchDto matchDto = convertToMatchDto(match);
        String message = "Successfully joined match: " + match.getName();
        Result<JoinMatchResponse> response = Result.success(new JoinMatchResponse(matchDto, message));

        MatchPlayerDto matchPlayerDto = convertToMatchPlayerDto(newPlayer);

        if (response.isSuccess()) {
            UserJoinedMatchEvent event = new UserJoinedMatchEvent(match.getId(), matchPlayerDto);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.USER_JOINED_MATCH, clientId, event);
            RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        }

        return response;
    }

    public Result<PlayerFailedEventResponse> playerFailed(PlayerFailedEventRequest request, String clientId) {
        PlayerFailedEvent event = request.getPlayerFailedEvent();
        int matchId = event.getMatchId();
        Match match = matches.get(matchId);

        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        MatchPlayer player = findPlayerInMatch(matchId, userId);
        if (player == null) {
            return Result.failure(Error.validation("Player not found in match"));
        }
        player.setStatus(PlayerStatus.FAILED);

        RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.PLAYER_FAILED_EVENT, clientId, event);
        broadcastMessageToMatchPlayers(clientId, matchId, realtimeMessage);

        return Result.success(new PlayerFailedEventResponse("Player " + event.getUser().getUsername()
                + " failed in match " + matchId));
    }

    public Result<LeaveMatchResponse> leaveMatch(LeaveMatchRequest request, String clientId) {
        int matchId = request.getMatchId();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        if (!isUserInMatch(matchId, userId)) {
            return Result.failure(Error.validation("You are not in this match"));
        }

        MatchPlayer player = findPlayerInMatch(matchId, userId);
        PlayerRole playerRole = player != null ? player.getRole() : PlayerRole.PLAYER;

        removePlayerFromMatch(matchId, userId);

        String message = "Successfully left match: " + match.getName();
        Set<MatchPlayer> remainingPlayers = matchPlayers.get(matchId);

        if (playerRole.equals(PlayerRole.HOST)) {
            handleHostLeaving(matchId, userId);
        } else {
            UserLeftMatchEvent event = new UserLeftMatchEvent(matchId, userId);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.USER_LEFT_MATCH, clientId, event);
            RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        }

        for (MatchPlayer remainingPlayer : remainingPlayers) {
            System.out.println("Remaining player id " + remainingPlayer.getUserId()
                    + ", status: " + remainingPlayer.getStatus());
        }
        if (remainingPlayers.isEmpty()) {
            removeMatch(matchId);
        }
        boolean playerExists = remainingPlayers.stream()
                .allMatch(p -> p.getStatus() != PlayerStatus.PLAYING);
        if (playerExists) {
            match.setInProgress(false);
            MatchCompletedEvent event = new MatchCompletedEvent(matchId);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.MATCH_COMPLETED, clientId, event);
            RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        }

        return Result.success(new LeaveMatchResponse(message));
    }

    public Result<KickPlayerResponse> kickPlayer(KickPlayerRequest request, String clientId) {
        int matchId = request.getMatchId();
        int playerToKickId = request.getUserId();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer kickingUserId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (kickingUserId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        MatchPlayer kickingPlayer = findPlayerInMatch(matchId, kickingUserId);
        if (kickingPlayer == null || !kickingPlayer.getRole().equals(PlayerRole.HOST)) {
            return Result.failure(Error.unauthorized("Only the host can kick players"));
        }

        MatchPlayer playerToKick = findPlayerInMatch(matchId, playerToKickId);
        if (playerToKick == null) {
            return Result.failure(Error.validation("Player is not in this match"));
        }

        if (kickingUserId.equals(playerToKickId)) {
            return Result.failure(Error.validation("You cannot kick yourself"));
        }

        removePlayerFromMatch(matchId, playerToKickId);

        User kickedUser = userService.findUserById(playerToKickId);
        String kickedUserName = kickedUser != null ? kickedUser.getUsername() : "Unknown";

        String message = "Player " + kickedUserName + " was kicked from the match";
        Result<KickPlayerResponse> response = Result.success(new KickPlayerResponse(message));

        if (response.isSuccess()) {
            PlayerKickedEvent event = new PlayerKickedEvent(matchId, playerToKickId);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.PLAYER_KICKED_FROM_MATCH, clientId, event);
            RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        }

        return response;
    }

    public Result<TransferHostResponse> transferHost(TransferHostRequest request, String clientId) {
        int matchId = request.getMatchId();
        int newHostUserId = request.getNewHostUserId();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer currentUserId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (currentUserId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        MatchPlayer currentPlayer = findPlayerInMatch(matchId, currentUserId);
        if (currentPlayer == null || !currentPlayer.getRole().equals(PlayerRole.HOST)) {
            return Result.failure(Error.unauthorized("Only the host can transfer host role"));
        }

        if (currentUserId.equals(newHostUserId)) {
            return Result.failure(Error.validation("You cannot transfer host to yourself"));
        }

        MatchPlayer newHostPlayer = findPlayerInMatch(matchId, newHostUserId);
        if (newHostPlayer == null) {
            return Result.failure(Error.validation("New host player is not in this match"));
        }

        if (newHostPlayer.getRole().equals(PlayerRole.HOST)) {
            return Result.failure(Error.validation("Player is already the host"));
        }

        newHostPlayer.setRole(PlayerRole.HOST);
        currentPlayer.setRole(PlayerRole.PLAYER);

        Result<TransferHostResponse> response = Result.success(new TransferHostResponse("Host transferred to user id " + newHostPlayer.getUserId()));
        if (response.isSuccess()) {
            HostChangedEvent event = new HostChangedEvent(matchId, newHostUserId, currentUserId);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.HOST_CHANGED, clientId, event);
            RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        }

        return response;
    }

    public Result<StartMatchResponse> startMatch(StartMatchRequest request, String clientId) {
        int matchId = request.getMatchId();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        MatchPlayer player = findPlayerInMatch(matchId, userId);
        if (player == null || !player.getRole().equals(PlayerRole.HOST)) {
            return Result.failure(Error.unauthorized("Only the host can start the match"));
        }

        if (match.isInProgress()) {
            return Result.failure(Error.validation("Match is already in progress"));
        }

        // Check if there's a beatmap selected
        if (match.getBeatmapId() <= 0) {
            return Result.failure(Error.validation("No beatmap selected for this match"));
        }

        Set<MatchPlayer> players = matchPlayers.get(matchId);
        if (players == null || players.size() < 2) {
            return Result.failure(Error.validation("At least 2 players are required to start the match"));
        }

        match.setInProgress(true);

//        for (MatchPlayer matchPlayer : players) {
//            if(matchPlayer.getStatus() == PlayerStatus.READY) matchPlayer.setStatus(PlayerStatus.PLAYING);
//        }

        MatchDto matchDto = convertToMatchDto(match);
        String message = "Match started: " + match.getName();
        Result<StartMatchResponse> response = Result.success(new StartMatchResponse(matchDto, message));

        if (response.isSuccess()) {
            MatchStartedEvent event = new MatchStartedEvent(matchId, matchDto);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.MATCH_STARTED, clientId, event);

            RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        }

        return response;
    }

    public Result<SendMatchScoreEventResponse> sendMatchScoreEvent(SendMatchScoreEventRequest request, String clientId) {
        MatchScoreEvent event = request.getMatchScoreEvent();
        int matchId = event.getMatchId();
        Match match = matches.get(matchId);

        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.MATCH_SCORE_EVENT, clientId, event);
        broadcastMessageToMatchPlayers(clientId, matchId, realtimeMessage);

        return Result.success(new SendMatchScoreEventResponse("Match score event sent successfully"));
    }

    public Result<PlayerFinishedEventResponse> playerFinishedMatch(PlayerFinishedEventRequest request, String clientId) {
        PlayerFinishedEvent event = request.getPlayerFinishedEvent();
        int matchId = event.getMatchId();
        int userId = event.getUser().getId();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        if (!isUserInMatch(matchId, userId)) {
            return Result.failure(Error.validation("You are not in this match"));
        }

        MatchPlayer player = findPlayerInMatch(matchId, userId);
        if (player == null) {
            return Result.failure(Error.validation("Player not found in match"));
        }

        player.setStatus(PlayerStatus.FINISHED);
        // check if all players have finished
        Set<MatchPlayer> players = matchPlayers.get(matchId);
        boolean allFinished = players.stream()
                .allMatch(p -> p.getStatus() == PlayerStatus.FINISHED);
        if (allFinished) {
            sendMatchCompletedEvent(matchId, clientId);
            match.setInProgress(false);
        }

        return Result.success(new PlayerFinishedEventResponse("Player finished match successfully"));
    }

    private void sendMatchCompletedEvent(int matchId, String clientId) {
        Match match = matches.get(matchId);
        if (match == null) return;

        MatchCompletedEvent event = new MatchCompletedEvent(matchId);
        RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.MATCH_COMPLETED, clientId, event);
        RealtimeMessageHandler.broadcastToAll(realtimeMessage);
    }

    public Result<ChangeMatchSlotResponse> changeMatchSlot(ChangeMatchSlotRequest request, String clientId) {
        int matchId = request.getMatchId();
        int newSlotIndex = request.getNewSlotIndex();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        if (!isUserInMatch(matchId, userId)) {
            return Result.failure(Error.validation("You are not in this match"));
        }

        MatchPlayer player = findPlayerInMatch(matchId, userId);
        if (player == null) {
            return Result.failure(Error.validation("Player not found in match"));
        }

        if (newSlotIndex < 0 || newSlotIndex >= match.getMaxPlayerCount()) {
            return Result.failure(Error.validation("Invalid slot index"));
        }

        Set<MatchPlayer> players = matchPlayers.get(matchId);
        boolean slotOccupied = players.stream()
                .anyMatch(p -> p.getSlotIndex() == newSlotIndex && p.getUserId() != userId);
        
        if (slotOccupied) {
            return Result.failure(Error.validation("Target slot is already occupied"));
        }

        int oldSlotIndex = player.getSlotIndex();
        
        if (oldSlotIndex == newSlotIndex) {
            return Result.failure(Error.validation("You are already in that slot"));
        }

        player.setSlotIndex(newSlotIndex);

        String message = "Successfully changed to slot " + (newSlotIndex + 1);
        Result<ChangeMatchSlotResponse> response = Result.success(new ChangeMatchSlotResponse(message));

        if (response.isSuccess()) {
            SlotChangedEvent event = new SlotChangedEvent(matchId, userId, oldSlotIndex, newSlotIndex);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.SLOT_CHANGED, clientId, event);
            RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        }

        return response;
    }

    public Result<UpdateMatchPasswordResponse> updateMatchPassword(UpdateMatchPasswordRequest request, String clientId) {
        int matchId = request.getMatchId();
        String newPassword = request.getNewPassword();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        MatchPlayer player = findPlayerInMatch(matchId, userId);
        if (player == null || !player.getRole().equals(PlayerRole.HOST)) {
            return Result.failure(Error.unauthorized("Only the host can update the match password"));
        }

        if (newPassword != null && newPassword.length() > 50) {
            return Result.failure(Error.validation("Password cannot be longer than 50 characters"));
        }

        if (newPassword != null && newPassword.trim().isEmpty()) {
            return Result.failure(Error.validation("Password cannot be empty. Use null to remove password"));
        }

        match.setPassword(newPassword);
        
        Result<UpdateMatchPasswordResponse> response = Result.success(new UpdateMatchPasswordResponse("Successfully updated match password!"));

        if (response.isSuccess()) {
            MatchPasswordUpdatedEvent event = new MatchPasswordUpdatedEvent(matchId);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.MATCH_PASSWORD_UPDATED, clientId, event);
            RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        }

        return response;
    }

    public Result<UpdateMatchNameResponse> updateMatchName(UpdateMatchNameRequest request, String clientId) {
        int matchId = request.getMatchId();
        String newName = request.getNewName();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        MatchPlayer player = findPlayerInMatch(matchId, userId);
        if (player == null || !player.getRole().equals(PlayerRole.HOST)) {
            return Result.failure(Error.unauthorized("Only the host can update the match name"));
        }

        if (newName == null || newName.trim().isEmpty()) {
            return Result.failure(Error.validation("Match name cannot be empty"));
        }

        String oldName = match.getName();
        match.setName(newName.trim());

        String message = "Match name updated from '" + oldName + "' to '" + newName.trim() + "'";
        Result<UpdateMatchNameResponse> response = Result.success(new UpdateMatchNameResponse(message));

        if (response.isSuccess()) {
            MatchNameUpdatedEvent event = new MatchNameUpdatedEvent(matchId, newName.trim());
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.MATCH_NAME_UPDATED, clientId, event);
            RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        }

        return response;
    }

    public Result<UpdateMatchBeatmapResponse> updateMatchBeatmap(UpdateMatchBeatmapRequest request, String clientId) {
        int matchId = request.getMatchId();
        int newBeatmapId = request.getNewBeatmapId();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        MatchPlayer player = findPlayerInMatch(matchId, userId);
        if (player == null || !player.getRole().equals(PlayerRole.HOST)) {
            return Result.failure(Error.unauthorized("Only the host can update the match beatmap"));
        }

        if (newBeatmapId <= 0) {
            return Result.failure(Error.validation("Invalid beatmap ID"));
        }

        GetBeatmapByIdRequest beatmapRequest = new GetBeatmapByIdRequest(newBeatmapId);
        Result<GetBeatmapByIdResponse> beatmapResult = beatmapService.getBeatmapById(beatmapRequest);

        if (!beatmapResult.isSuccess()) {
            return Result.failure(Error.notFound("Beatmap not found with ID: " + newBeatmapId));
        }

        BeatmapDto beatmapDto = beatmapResult.getValue().getBeatmap();
        match.setBeatmapId(newBeatmapId);

        String message = "Match beatmap updated to " + beatmapDto.getBeatmapSetDto().getTitle() + " [" + beatmapDto.getVersion() + "]";
        Result<UpdateMatchBeatmapResponse> response = Result.success(new UpdateMatchBeatmapResponse(message));

        if (response.isSuccess()) {
            MatchBeatmapUpdatedEvent event = new MatchBeatmapUpdatedEvent(matchId, beatmapDto);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.MATCH_BEATMAP_UPDATED, clientId, event);
            RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        }

        return response;
    }

    public Result<UpdateMatchChangingBeatmapResponse> updateMatchChangingBeatmap(UpdateMatchChangingBeatmapRequest request, String clientId) {
        int matchId = request.getMatchId();
        boolean isChangingBeatmap = request.isChangingBeatmap();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        MatchPlayer player = findPlayerInMatch(matchId, userId);
        if (player == null || !player.getRole().equals(PlayerRole.HOST)) {
            return Result.failure(Error.unauthorized("Only the host can update the changing beatmap status"));
        }

        match.setChangingBeatmap(isChangingBeatmap);

        String message = "Match changing beatmap status updated to " + (isChangingBeatmap ? "true" : "false");
        Result<UpdateMatchChangingBeatmapResponse> response = Result.success(new UpdateMatchChangingBeatmapResponse(message));

        if (response.isSuccess()) {
            MatchChangingBeatmapUpdatedEvent event = new MatchChangingBeatmapUpdatedEvent(matchId, isChangingBeatmap);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.MATCH_CHANGING_BEATMAP_UPDATED, clientId, event);
            RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        }

        return response;
    }

    public Result<UpdateMatchWinConditionResponse> updateMatchWinCondition(UpdateMatchWinConditionRequest request, String clientId) {
        int matchId = request.getMatchId();
        MatchWinCondition newWinCondition = request.getNewWinCondition();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        MatchPlayer player = findPlayerInMatch(matchId, userId);
        if (player == null || !player.getRole().equals(PlayerRole.HOST)) {
            return Result.failure(Error.unauthorized("Only the host can update the match win condition"));
        }

        if (newWinCondition == null) {
            return Result.failure(Error.validation("Win condition cannot be null"));
        }

        MatchWinCondition oldWinCondition = match.getWinCondition();
        match.setWinCondition(newWinCondition);

        String message = "Match win condition updated from '" + oldWinCondition.getDisplayName() + "' to '" + newWinCondition.getDisplayName() + "'";
        Result<UpdateMatchWinConditionResponse> response = Result.success(new UpdateMatchWinConditionResponse(message));

        if (response.isSuccess()) {
            MatchWinConditionUpdatedEvent event = new MatchWinConditionUpdatedEvent(matchId, newWinCondition);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.MATCH_WIN_CONDITION_UPDATED, clientId, event);
            RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        }

        return response;
    }

    public Result<UpdatePlayerStatusResponse> updatePlayerStatus(UpdatePlayerStatusRequest request, String clientId) {
        int matchId = request.getMatchId();
        PlayerStatus newStatus = request.getNewStatus();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer userId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (userId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        if (!isUserInMatch(matchId, userId)) {
            return Result.failure(Error.validation("You are not in this match"));
        }

        MatchPlayer player = findPlayerInMatch(matchId, userId);
        if (player == null) {
            return Result.failure(Error.notFound("Player not found in match"));
        }

        if (newStatus == null) {
            return Result.failure(Error.validation("Player status cannot be null"));
        }

        PlayerStatus oldStatus = player.getStatus();
        
        if (oldStatus == newStatus) {
            return Result.failure(Error.validation("Player status is already " + newStatus.name()));
        }

        player.setStatus(newStatus);

        String message = "Player status updated to " + newStatus.name();
        Result<UpdatePlayerStatusResponse> response = Result.success(new UpdatePlayerStatusResponse(message));

        if (response.isSuccess()) {
            PlayerStatusUpdatedEvent event = new PlayerStatusUpdatedEvent(matchId, userId, newStatus);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.PLAYER_STATUS_UPDATED, clientId, event);
            broadcastMessageToMatchPlayers(clientId, matchId, realtimeMessage);
        }

        return response;
    }

    private MatchDto convertToMatchDto(Match match) {
        List<MatchPlayerDto> playerDtos = new ArrayList<>();
        Set<MatchPlayer> matchPlayerSet = matchPlayers.getOrDefault(match.getId(), Collections.emptySet());

        for (MatchPlayer player : matchPlayerSet) {
            User user = userService.findUserById(player.getUserId());
            if (user != null) {
                UserDto userDto = new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getCountryCode(),
                        user.getProfilePicture(),
                        user.getPerformance(),
                        user.getAccuracy(),
                        user.getPlayCount(),
                        user.getLevel(),
                        userService.getUserRank(user.getId()),
                        user.isSupporter()
                );

                MatchPlayerDto playerDto = new MatchPlayerDto(
                        player.getId(),
                        match.getId(),
                        player.getUserId(),
                        userDto,
                        player.getRole(),
                        player.getStatus(),
                        player.getSlotIndex()
                );
                playerDtos.add(playerDto);
            }
        }

        int beatmapId = match.getBeatmapId();
        GetBeatmapByIdRequest getBeatmapByIdRequest = new GetBeatmapByIdRequest(beatmapId);
        Result<GetBeatmapByIdResponse> beatmapResult = beatmapService.getBeatmapById(getBeatmapByIdRequest);

        if (!beatmapResult.isSuccess()) {
            return null;
        }

        BeatmapDto beatmap = beatmapResult.getValue().getBeatmap();

        int lowestRank = 0;
        int highestRank = 0;
        if (!playerDtos.isEmpty()) {
            List<Integer> ranks = playerDtos.stream()
                    .map(p -> p.getUser().getRank())
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
            lowestRank = ranks.get(ranks.size() - 1);
            highestRank = ranks.get(0);
        }

        return new MatchDto(
                match.getId(),
                match.getName(),
                match.getPassword(),
                match.isInProgress(),
                match.isChangingBeatmap(),
                match.getMaxPlayerCount(),
                beatmap,
                lowestRank,
                highestRank,
                match.getWinCondition(),
                playerDtos
        );
    }

    private MatchPlayerDto convertToMatchPlayerDto(MatchPlayer player) {
        User user = userService.findUserById(player.getUserId());
        if (user == null) {
            return null;
        }

        UserDto userDto = new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCountryCode(),
                user.getProfilePicture(),
                user.getPerformance(),
                user.getAccuracy(),
                user.getPlayCount(),
                user.getLevel(),
                userService.getUserRank(user.getId()),
                user.isSupporter()
        );

        return new MatchPlayerDto(
                player.getId(),
                player.getMatchId(),
                player.getUserId(),
                userDto,
                player.getRole(),
                player.getStatus(),
                player.getSlotIndex()
        );
    }

    private int findAvailableSlot(int matchId) {
        Match match = matches.get(matchId);
        if (match == null) {
            return -1;
        }
        
        Set<MatchPlayer> players = matchPlayers.get(matchId);
        Set<Integer> occupiedSlots = new HashSet<>();
        
        if (players != null) {
            for (MatchPlayer player : players) {
                occupiedSlots.add(player.getSlotIndex());
            }
        }

        for (int i = 0; i < match.getMaxPlayerCount(); i++) {
            if (!occupiedSlots.contains(i)) {
                return i;
            }
        }
        return -1;
    }

    private void handleHostLeaving(int matchId, int previousHostUserId) {
        Set<MatchPlayer> players = matchPlayers.get(matchId);
        
        if (players != null && !players.isEmpty()) {
            MatchPlayer newHost = players.iterator().next();
            newHost.setRole(PlayerRole.HOST);

            HostLeftEvent hostLeftEvent = new HostLeftEvent(matchId, previousHostUserId, newHost.getUserId());
            RealtimeMessage hostLeftMessage = new RealtimeMessage(RealtimeMessageType.HOST_LEFT, "SYSTEM", hostLeftEvent);
            RealtimeMessageHandler.broadcastToAll(hostLeftMessage);
        } else {
            removeMatch(matchId);
        }
    }

    private void removeMatch(int matchId) {
        Set<MatchPlayer> players = matchPlayers.get(matchId);
        if (players != null) {
            for (MatchPlayer player : players) {
                userToMatch.remove(player.getUserId());
            }
        }
        
        MatchEndedEvent event = new MatchEndedEvent(matchId);
        RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.MATCH_ENDED, "SYSTEM", event);
        RealtimeMessageHandler.broadcastToAll(realtimeMessage);
        
        matches.remove(matchId);
        matchPlayers.remove(matchId);
    }

    private void broadcastMessageToMatchPlayers(String clientId, int matchId, RealtimeMessage realtimeMessage) {
        Set<MatchPlayer> players = matchPlayers.get(matchId);
        if (players != null) {
            for (MatchPlayer player : players) {
                String playerClientId = sessionService.getClientIdByUserId(player.getUserId());
                if (playerClientId != null) {
                    RealtimeMessageHandler.sendToClient(realtimeMessage, playerClientId);
                }
            }
        }
    }

    public Set<Integer> getMatchPlayers(int matchId) {
        Set<MatchPlayer> players = matchPlayers.getOrDefault(matchId, Collections.emptySet());
        Set<Integer> playerIds = new HashSet<>();
        for (MatchPlayer player : players) {
            playerIds.add(player.getUserId());
        }
        return playerIds;
    }

    public Integer getCurrentMatchForUser(int userId) {
        return userToMatch.get(userId);
    }

    public void removeUserFromAllMatches(int userId) {
        Integer matchId = userToMatch.get(userId);
        if (matchId != null) {
            Set<MatchPlayer> players = matchPlayers.get(matchId);
            if (players != null) {
                MatchPlayer userPlayer = findPlayerInMatch(matchId, userId);
                boolean wasHost = userPlayer != null && PlayerRole.HOST.equals(userPlayer.getRole());
                
                removePlayerFromMatch(matchId, userId);
                
                if (wasHost) {
                    handleHostLeaving(matchId, userId);
                } else {
                    Set<MatchPlayer> remainingPlayers = matchPlayers.get(matchId);
                    if (remainingPlayers == null || remainingPlayers.isEmpty()) {
                        removeMatch(matchId);
                    }
                }
            }
        }
    }

    public Match getMatchById(int matchId) {
        return matches.get(matchId);
    }
}