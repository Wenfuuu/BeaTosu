package beat.osu.server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import beat.osu.server.entities.BeatmapSet;
import beat.osu.server.entities.Match;
import beat.osu.server.entities.MatchPlayer;
import beat.osu.server.entities.User;
import beat.osu.server.handler.RealtimeMessageHandler;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.match.MatchDto;
import beat.osu.shared.dto.match.MatchPlayerDto;
import beat.osu.shared.dto.match.events.MatchCreatedEvent;
import beat.osu.shared.dto.match.events.PlayerKickedEvent;
import beat.osu.shared.dto.match.events.UserJoinedMatchEvent;
import beat.osu.shared.dto.match.events.UserLeftMatchEvent;
import beat.osu.shared.dto.match.requests.CreateMatchRequest;
import beat.osu.shared.dto.match.requests.JoinMatchRequest;
import beat.osu.shared.dto.match.requests.KickPlayerRequest;
import beat.osu.shared.dto.match.requests.LeaveMatchRequest;
import beat.osu.shared.dto.match.responses.CreateMatchResponse;
import beat.osu.shared.dto.match.responses.GetAllMatchesResponse;
import beat.osu.shared.dto.match.responses.JoinMatchResponse;
import beat.osu.shared.dto.match.responses.KickPlayerResponse;
import beat.osu.shared.dto.match.responses.LeaveMatchResponse;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.enums.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;

public class MatchService {

    private final Map<Integer, Match> matches = new ConcurrentHashMap<>();
    private final Map<Integer, Set<MatchPlayer>> matchPlayers = new ConcurrentHashMap<>(); // matchId -> players
    private final Map<Integer, Set<Integer>> userMatches = new ConcurrentHashMap<>(); // userId -> matchIds

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
        Set<MatchPlayer> players = matchPlayers.get(matchId);
        if (players == null) return false;
        return players.stream().anyMatch(player -> player.getUserId() == userId);
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

        Set<Integer> userMatchIds = getUserMatches(userId);
        if (!userMatchIds.isEmpty()) {
            return Result.failure(Error.validation("You are already in a match"));
        }

        int matchId = matchIdGenerator.getAndIncrement();
        Match match = new Match(
                matchId,
                request.getName().trim(),
                request.getPassword(),
                "waiting",
                request.getMaxPlayerCount(),
                0,
                "score"
        );

        matches.put(matchId, match);
        matchPlayers.put(matchId, ConcurrentHashMap.newKeySet());

        int hostPlayerId = matchPlayerIdGenerator.getAndIncrement();
        MatchPlayer hostPlayer = new MatchPlayer(hostPlayerId, matchId, userId, "host", "ready", 0);
        matchPlayers.get(matchId).add(hostPlayer);
        userMatches.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(matchId);

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

        Set<Integer> userMatchIds = getUserMatches(userId);
        if (!userMatchIds.isEmpty()) {
            return Result.failure(Error.validation("You are already in a match"));
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
        MatchPlayer newPlayer = new MatchPlayer(newPlayerId, matchId, userId, "player", "not_ready", availableSlot);
        matchPlayers.get(matchId).add(newPlayer);
        userMatches.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(matchId);

        MatchDto matchDto = convertToMatchDto(match);
        String message = "Successfully joined match: " + match.getName();
        Result<JoinMatchResponse> response = Result.success(new JoinMatchResponse(matchDto, message));

        MatchPlayerDto matchPlayerDto = convertToMatchPlayerDto(newPlayer);

        if (response.isSuccess()) {
            UserJoinedMatchEvent event = new UserJoinedMatchEvent(match.getId(), matchPlayerDto);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.USER_JOINED_MATCH, clientId, event);
            broadcastMessageToMatchPlayers(clientId, matchId, realtimeMessage);
        }

        return response;
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
        String playerRole = player != null ? player.getRole() : "player";

        removePlayerFromMatch(matchId, userId);
        Set<Integer> userMatchSet = userMatches.get(userId);
        if (userMatchSet != null) {
            userMatchSet.remove(matchId);
        }

        String message = "Successfully left match: " + match.getName();

        if ("host".equals(playerRole)) {
            handleHostLeaving(matchId);  // transfer host role
        }

        if (matchPlayers.get(matchId).isEmpty()) {
            removeMatch(matchId);
        }

        Result<LeaveMatchResponse> response = Result.success(new LeaveMatchResponse(message));

        if (response.isSuccess()) {
            UserLeftMatchEvent event = new UserLeftMatchEvent(matchId, userId);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.USER_LEFT_MATCH, clientId, event);
            broadcastMessageToMatchPlayers(clientId, matchId, realtimeMessage);
        }

        return response;
    }

    public Result<KickPlayerResponse> kickPlayer(KickPlayerRequest request, String clientId) {
        int matchId = request.getMatchId();
        int playerToKickId = request.getPlayerId();

        Match match = matches.get(matchId);
        if (match == null) {
            return Result.failure(Error.notFound("Match not found"));
        }

        Integer kickingUserId = (Integer) sessionService.getSessionValue(clientId, "userId");
        if (kickingUserId == null) {
            return Result.failure(Error.unauthorized("User not authenticated"));
        }

        MatchPlayer kickingPlayer = findPlayerInMatch(matchId, kickingUserId);
        if (kickingPlayer == null || !"host".equals(kickingPlayer.getRole())) {
            return Result.failure(Error.unauthorized("Only the host can kick players"));
        }

        if (!isUserInMatch(matchId, playerToKickId)) {
            return Result.failure(Error.validation("Player is not in this match"));
        }

        if (kickingUserId.equals(playerToKickId)) {
            return Result.failure(Error.validation("You cannot kick yourself"));
        }

        removePlayerFromMatch(matchId, playerToKickId);
        Set<Integer> userMatchSet = userMatches.get(playerToKickId);
        if (userMatchSet != null) {
            userMatchSet.remove(matchId);
        }

        User kickedUser = userService.findUserById(playerToKickId);
        String kickedUserName = kickedUser != null ? kickedUser.getUsername() : "Unknown";

        String message = "Player " + kickedUserName + " was kicked from the match";
        Result<KickPlayerResponse> response = Result.success(new KickPlayerResponse(message));

        if (response.isSuccess()) {
            PlayerKickedEvent event = new PlayerKickedEvent(matchId, playerToKickId);
            RealtimeMessage realtimeMessage = new RealtimeMessage(RealtimeMessageType.PLAYER_KICKED_FROM_MATCH, clientId, event);
            broadcastMessageToMatchPlayers(clientId, matchId, realtimeMessage);

            String kickedPlayerClientId = sessionService.getClientIdByUserId(playerToKickId);
            if (kickedPlayerClientId != null) {
                RealtimeMessageHandler.sendToClient(realtimeMessage, kickedPlayerClientId);
            }
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

        String beatmapName = "No beatmap selected";
        if (match.getBeatmapId() > 0) {
            BeatmapSet beatmapSet = beatmapService.getBeatmapSetById(match.getBeatmapId());
            if (beatmapSet != null) {
                beatmapName = beatmapSet.getTitle();
            }
        }

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
                match.getStatus(),
                match.getMaxPlayerCount(),
                match.getBeatmapId(),
                beatmapName,
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

    private void handleHostLeaving(int matchId) {
        Set<MatchPlayer> players = matchPlayers.get(matchId);
        if (players != null && !players.isEmpty()) {
            MatchPlayer newHost = players.iterator().next();
            newHost.setRole("host");
        }
    }

    private void removeMatch(int matchId) {
        matches.remove(matchId);
        matchPlayers.remove(matchId);
    }

    private void broadcastMessageToMatchPlayers(String clientId, int matchId, RealtimeMessage realtimeMessage) {
        Set<MatchPlayer> players = matchPlayers.get(matchId);
        if (players != null) {
            for (MatchPlayer player : players) {
                String playerClientId = sessionService.getClientIdByUserId(player.getUserId());
                if (playerClientId != null && !playerClientId.equals(clientId)) {
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

    public Set<Integer> getUserMatches(int userId) {
        return new HashSet<>(userMatches.getOrDefault(userId, Collections.emptySet()));
    }

    public void removeUserFromAllMatches(int userId) {
        Set<Integer> userMatchSet = userMatches.remove(userId);
        if (userMatchSet != null) {
            for (int matchId : userMatchSet) {
                Set<MatchPlayer> players = matchPlayers.get(matchId);
                if (players != null) {
                    MatchPlayer userPlayer = findPlayerInMatch(matchId, userId);
                    boolean wasHost = userPlayer != null && "host".equals(userPlayer.getRole());
                    
                    removePlayerFromMatch(matchId, userId);
                    
                    if (wasHost) {
                        handleHostLeaving(matchId);
                    }
                    
                    if (players.isEmpty()) {
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