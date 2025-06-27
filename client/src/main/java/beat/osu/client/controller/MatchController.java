package beat.osu.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.match.MatchDto;
import beat.osu.shared.dto.match.MatchPlayerDto;
import beat.osu.shared.dto.match.events.*;
import beat.osu.shared.dto.match.requests.*;
import beat.osu.shared.dto.match.responses.*;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.enums.match.PlayerRole;
import beat.osu.shared.enums.message.MessageAction;
import beat.osu.shared.enums.message.MessageType;
import beat.osu.shared.enums.message.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;
import beat.osu.shared.models.RequestMessage;
import lombok.Getter;

public class MatchController {

    private final ClientService clientService;

    @Getter
    private List<MatchDto> matches = new ArrayList<>();

    private final List<Consumer<MatchCreatedEvent>> matchCreatedCallbacks = new ArrayList<>();
    private final List<Consumer<MatchEndedEvent>> matchEndedCallbacks = new ArrayList<>();
    private final List<Consumer<UserJoinedMatchEvent>> userJoinedMatchCallbacks = new ArrayList<>();
    private final List<Consumer<UserLeftMatchEvent>> userLeftMatchCallbacks = new ArrayList<>();
    private final List<Consumer<PlayerKickedEvent>> playerKickedCallbacks = new ArrayList<>();
    private final List<Consumer<HostChangedEvent>> hostChangedCallbacks = new ArrayList<>();
    private final List<Consumer<HostLeftEvent>> hostLeftCallbacks = new ArrayList<>();
    private final List<Consumer<MatchStartedEvent>> matchStartedCallbacks = new ArrayList<>();
    private final List<Consumer<MatchScoreEvent>> matchScoreCallbacks = new ArrayList<>();

    public MatchController() {
        this.clientService = ClientService.getInstance();
        requestMatches();
        setupRealtimeHandler();
    }

    public void addMatchCreatedCallback(Consumer<MatchCreatedEvent> callback) {
        matchCreatedCallbacks.add(callback);
    }

    public void addMatchEndedCallback(Consumer<MatchEndedEvent> callback) {
        matchEndedCallbacks.add(callback);
    }

    public void addUserJoinedMatchCallback(Consumer<UserJoinedMatchEvent> callback) {
        userJoinedMatchCallbacks.add(callback);
    }

    public void addUserLeftMatchCallback(Consumer<UserLeftMatchEvent> callback) {
        userLeftMatchCallbacks.add(callback);
    }

    public void addPlayerKickedCallback(Consumer<PlayerKickedEvent> callback) {
        playerKickedCallbacks.add(callback);
    }

    public void addHostChangedCallback(Consumer<HostChangedEvent> callback) {
        hostChangedCallbacks.add(callback);
    }

    public void addHostLeftCallback(Consumer<HostLeftEvent> callback) {
        hostLeftCallbacks.add(callback);
    }

    public void addMatchStartedCallback(Consumer<MatchStartedEvent> callback) {
        matchStartedCallbacks.add(callback);
    }

    public void addMatchScoreCallback(Consumer<MatchScoreEvent> callback) {
        matchScoreCallbacks.add(callback);
    }

    public void removeMatchCreatedCallback(Consumer<MatchCreatedEvent> callback) {
        matchCreatedCallbacks.remove(callback);
    }

    public void removeMatchEndedCallback(Consumer<MatchEndedEvent> callback) {
        matchEndedCallbacks.remove(callback);
    }

    public void removeUserJoinedMatchCallback(Consumer<UserJoinedMatchEvent> callback) {
        userJoinedMatchCallbacks.remove(callback);
    }

    public void removeUserLeftMatchCallback(Consumer<UserLeftMatchEvent> callback) {
        userLeftMatchCallbacks.remove(callback);
    }

    public void removePlayerKickedCallback(Consumer<PlayerKickedEvent> callback) {
        playerKickedCallbacks.remove(callback);
    }

    public void removeHostChangedCallback(Consumer<HostChangedEvent> callback) {
        hostChangedCallbacks.remove(callback);
    }

    public void removeHostLeftCallback(Consumer<HostLeftEvent> callback) {
        hostLeftCallbacks.remove(callback);
    }

    public void removeMatchStartedCallback(Consumer<MatchStartedEvent> callback) {
        matchStartedCallbacks.remove(callback);
    }

    public void removeMatchScoreCallback(Consumer<MatchScoreEvent> callback) {
        matchScoreCallbacks.remove(callback);
    }

    private void setupRealtimeHandler() {
        if (clientService.getConnection() != null && clientService.getConnection().getRealtimeHandler() != null) {
            clientService.getConnection().getRealtimeHandler().addCallback(this::handleRealtimeMessage);
        }
    }

    private void requestMatches() {
        if (clientService.getConnection() != null && clientService.getConnection().isConnected()) {
            RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.GET_ALL_MATCHES, null);

            clientService.getConnection().sendRequest(request).thenAccept(response -> {
                try {
                    Result<?> result = (Result<?>) response;
                    if (result.isSuccess()) {
                        GetAllMatchesResponse getAllMatchesResponse = (GetAllMatchesResponse) result.getValue();
                        matches = getAllMatchesResponse.getMatches();
                    }
                } catch (Exception e) {
                    System.err.println("Error processing get all matches response: " + e.getMessage());
                }
            }).exceptionally(throwable -> {
                System.err.println("Error requesting matches: " + throwable.getMessage());
                return null;
            });
        }
    }

    public CompletableFuture<Result<CreateMatchResponse>> createMatch(String gameName, String password, int maxPlayers) {
         CreateMatchRequest requestData =  new CreateMatchRequest(gameName, password, maxPlayers);
         RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.CREATE_MATCH, requestData);

         return CompletableFuture.supplyAsync(() -> {
             try {
                 Object response = clientService.getConnection().sendRequest(request).get();

                 Result<?> result = (Result<?>) response;
                 if (result.isSuccess()) {
                     return Result.success((CreateMatchResponse) result.getValue());
                 } else {
                     return Result.failure(result.getError());
                 }
             } catch (Exception e) {
                 return Result.failure(Error.network(e.getMessage()));
             }
         });
    }

    public CompletableFuture<Result<JoinMatchResponse>> joinMatch(int matchId, String password) {
        JoinMatchRequest requestData = new JoinMatchRequest(matchId, password);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.JOIN_MATCH, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((JoinMatchResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<LeaveMatchResponse>> leaveMatch(int matchId) {
        LeaveMatchRequest requestData = new LeaveMatchRequest(matchId);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.LEAVE_MATCH, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((LeaveMatchResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<StartMatchResponse>> startMatch(int matchId) {
        StartMatchRequest requestData = new StartMatchRequest(matchId);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.START_MATCH, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((StartMatchResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<SendMatchScoreEventResponse>> sendMatchScoreEvent(MatchScoreEvent event) {
        SendMatchScoreEventRequest requestData = new SendMatchScoreEventRequest(event);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.SEND_MATCH_SCORE_EVENT, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((SendMatchScoreEventResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    private void handleRealtimeMessage(RealtimeMessage message) {
        if (message.getType() == RealtimeMessageType.MATCH_CREATED) {
            if (message.getPayload() instanceof MatchCreatedEvent) {
                MatchCreatedEvent event = (MatchCreatedEvent) message.getPayload();
                matches.add(event.getMatch());
                notifyMatchCreated(event);
            }
        } else if (message.getType() == RealtimeMessageType.USER_JOINED_MATCH) {
            if (message.getPayload() instanceof UserJoinedMatchEvent) {
                UserJoinedMatchEvent event = (UserJoinedMatchEvent) message.getPayload();
                addPlayerToMatch(event.getMatchId(), event.getMatchPlayer());
                notifyUserJoinedMatch(event);
            }
        } else if (message.getType() == RealtimeMessageType.USER_LEFT_MATCH) {
            if (message.getPayload() instanceof UserLeftMatchEvent) {
                UserLeftMatchEvent event = (UserLeftMatchEvent) message.getPayload();
                removePlayerFromMatch(event.getMatchId(), event.getUserId());
                notifyUserLeftMatch(event);
            }
        } else if (message.getType() == RealtimeMessageType.PLAYER_KICKED_FROM_MATCH) {
            if (message.getPayload() instanceof PlayerKickedEvent) {
                PlayerKickedEvent event = (PlayerKickedEvent) message.getPayload();
                removePlayerFromMatch(event.getMatchId(), event.getKickedUserId());
                notifyPlayerKicked(event);
            }
        } else if (message.getType() == RealtimeMessageType.MATCH_ENDED) {
            if (message.getPayload() instanceof MatchEndedEvent) {
                MatchEndedEvent event = (MatchEndedEvent) message.getPayload();
                removeMatchFromList(event.getMatchId());
                notifyMatchEnded(event);
            }
        } else if (message.getType() == RealtimeMessageType.HOST_CHANGED) {
            if (message.getPayload() instanceof HostChangedEvent) {
                HostChangedEvent event = (HostChangedEvent) message.getPayload();
                updateMatchHost(event.getMatchId(), event.getNewHostUserId());
                notifyHostChanged(event);
            }
        } else if (message.getType() == RealtimeMessageType.HOST_LEFT) {
            if (message.getPayload() instanceof HostLeftEvent) {
                HostLeftEvent event = (HostLeftEvent) message.getPayload();
                handleHostLeft(event.getMatchId(), event.getPreviousHostUserId(), event.getNewHostUserId());
                notifyHostLeft(event);
            }
        } else if (message.getType() == RealtimeMessageType.MATCH_SCORE_EVENT) {
            if (message.getPayload() instanceof MatchScoreEvent) {
                MatchScoreEvent event = (MatchScoreEvent) message.getPayload();
                notifyMatchScoreEvent(event);
            }
        } else if (message.getType() == RealtimeMessageType.MATCH_STARTED) {
            if (message.getPayload() instanceof MatchStartedEvent) {
                MatchStartedEvent event = (MatchStartedEvent) message.getPayload();
                notifyMatchStarted(event);
            }
        } else if (message.getType() == RealtimeMessageType.MATCH_COMPLETED) {

        }
    }

    private void notifyMatchCreated(MatchCreatedEvent event) {
        for (Consumer<MatchCreatedEvent> callback : matchCreatedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in match created callback: " + e.getMessage());
            }
        }
    }

    private void notifyUserJoinedMatch(UserJoinedMatchEvent event) {
        for (Consumer<UserJoinedMatchEvent> callback : userJoinedMatchCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in user joined match callback: " + e.getMessage());
            }
        }
    }

    private void notifyUserLeftMatch(UserLeftMatchEvent event) {
        for (Consumer<UserLeftMatchEvent> callback : userLeftMatchCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in user left match callback: " + e.getMessage());
            }
        }
    }

    private void notifyPlayerKicked(PlayerKickedEvent event) {
        for (Consumer<PlayerKickedEvent> callback : playerKickedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in player kicked callback: " + e.getMessage());
            }
        }
    }

    private void notifyMatchEnded(MatchEndedEvent event) {
        for (Consumer<MatchEndedEvent> callback : matchEndedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in match ended callback: " + e.getMessage());
            }
        }
    }

    private void notifyHostChanged(HostChangedEvent event) {
        for (Consumer<HostChangedEvent> callback : hostChangedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in host changed callback: " + e.getMessage());
            }
        }
    }

    private void addPlayerToMatch(int matchId, MatchPlayerDto player) {
        for (MatchDto match : matches) {
            if (match.getId() == matchId) {
                match.getPlayers().add(player);
                return;
            }
        }
        System.err.println("Match with ID " + matchId + " not found to add player.");
    }

    private void removePlayerFromMatch(int matchId, int userId) {
        for (MatchDto match : matches) {
            if (match.getId() == matchId) {
                match.getPlayers().removeIf(p -> p.getUserId() == userId);
                return;
            }
        }
        System.err.println("Match with ID " + matchId + " not found to remove player.");
    }

    private void removeMatchFromList(int matchId) {
        matches.removeIf(match -> match.getId() == matchId);
    }

    private void updateMatchHost(int matchId, int newHostUserId) {
        for (MatchDto match : matches) {
            if (match.getId() == matchId) {
                for (MatchPlayerDto player : match.getPlayers()) {
                    if (player.getUserId() == newHostUserId) {
                        player.setRole(PlayerRole.HOST);
                    } else {
                        player.setRole(PlayerRole.PLAYER);
                    }
                }
                return;
            }
        }
        System.err.println("Match with ID " + matchId + " not found to update host.");
    }

    private void handleHostLeft(int matchId, int previousHostUserId, int newHostUserId) {
        for (MatchDto match : matches) {
            if (match.getId() == matchId) {
                match.getPlayers().removeIf(p -> p.getUserId() == previousHostUserId);
                
                for (MatchPlayerDto player : match.getPlayers()) {
                    if (player.getUserId() == newHostUserId) {
                        player.setRole(PlayerRole.HOST);
                    } else {
                        player.setRole(PlayerRole.PLAYER);
                    }
                }
                return;
            }
        }
        System.err.println("Match with ID " + matchId + " not found to handle host left.");
    }

    private void notifyHostLeft(HostLeftEvent event) {
        for (Consumer<HostLeftEvent> callback : hostLeftCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in host left callback: " + e.getMessage());
            }
        }
    }

    private void notifyMatchStarted(MatchStartedEvent event) {
        for (Consumer<MatchStartedEvent> callback : matchStartedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in match started callback: " + e.getMessage());
            }
        }
    }

    private void notifyMatchScoreEvent(MatchScoreEvent event) {
        for (Consumer<MatchScoreEvent> callback : matchScoreCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in match score event callback: " + e.getMessage());
            }
        }
    }
}