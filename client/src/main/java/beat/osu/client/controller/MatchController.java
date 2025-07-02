package beat.osu.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.BeatmapDto;
import beat.osu.shared.dto.match.MatchDto;
import beat.osu.shared.dto.match.MatchPlayerDto;
import beat.osu.shared.dto.match.events.HostChangedEvent;
import beat.osu.shared.dto.match.events.HostLeftEvent;
import beat.osu.shared.dto.match.events.MatchBeatmapUpdatedEvent;
import beat.osu.shared.dto.match.events.MatchChangingBeatmapUpdatedEvent;
import beat.osu.shared.dto.match.events.MatchCompletedEvent;
import beat.osu.shared.dto.match.events.MatchCreatedEvent;
import beat.osu.shared.dto.match.events.MatchEndedEvent;
import beat.osu.shared.dto.match.events.MatchNameUpdatedEvent;
import beat.osu.shared.dto.match.events.MatchPasswordUpdatedEvent;
import beat.osu.shared.dto.match.events.MatchScoreEvent;
import beat.osu.shared.dto.match.events.MatchStartedEvent;
import beat.osu.shared.dto.match.events.MatchWinConditionUpdatedEvent;
import beat.osu.shared.dto.match.events.PlayerFinishedEvent;
import beat.osu.shared.dto.match.events.PlayerKickedEvent;
import beat.osu.shared.dto.match.events.PlayerStatusUpdatedEvent;
import beat.osu.shared.dto.match.events.SlotChangedEvent;
import beat.osu.shared.dto.match.events.UserJoinedMatchEvent;
import beat.osu.shared.dto.match.events.UserLeftMatchEvent;
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
import beat.osu.shared.dto.match.responses.ChangeMatchSlotResponse;
import beat.osu.shared.dto.match.responses.CreateMatchResponse;
import beat.osu.shared.dto.match.responses.GetAllMatchesResponse;
import beat.osu.shared.dto.match.responses.JoinMatchResponse;
import beat.osu.shared.dto.match.responses.KickPlayerResponse;
import beat.osu.shared.dto.match.responses.LeaveMatchResponse;
import beat.osu.shared.dto.match.responses.PlayerFinishedEventResponse;
import beat.osu.shared.dto.match.responses.SendMatchScoreEventResponse;
import beat.osu.shared.dto.match.responses.StartMatchResponse;
import beat.osu.shared.dto.match.responses.TransferHostResponse;
import beat.osu.shared.dto.match.responses.UpdateMatchBeatmapResponse;
import beat.osu.shared.dto.match.responses.UpdateMatchChangingBeatmapResponse;
import beat.osu.shared.dto.match.responses.UpdateMatchNameResponse;
import beat.osu.shared.dto.match.responses.UpdateMatchPasswordResponse;
import beat.osu.shared.dto.match.responses.UpdateMatchWinConditionResponse;
import beat.osu.shared.dto.match.responses.UpdatePlayerStatusResponse;
import beat.osu.shared.enums.match.MatchWinCondition;
import beat.osu.shared.enums.match.PlayerRole;
import beat.osu.shared.enums.match.PlayerStatus;
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
    private final List<Consumer<MatchCompletedEvent>> matchCompletedCallbacks = new ArrayList<>();
    private final List<Consumer<SlotChangedEvent>> slotChangedCallbacks = new ArrayList<>();
    private final List<Consumer<MatchPasswordUpdatedEvent>> matchPasswordUpdatedCallbacks = new ArrayList<>();
    private final List<Consumer<MatchNameUpdatedEvent>> matchNameUpdatedCallbacks = new ArrayList<>();
    private final List<Consumer<MatchBeatmapUpdatedEvent>> matchBeatmapUpdatedCallbacks = new ArrayList<>();
    private final List<Consumer<MatchChangingBeatmapUpdatedEvent>> matchChangingBeatmapUpdatedCallbacks = new ArrayList<>();
    private final List<Consumer<MatchWinConditionUpdatedEvent>> matchWinConditionUpdatedCallbacks = new ArrayList<>();
    private final List<Consumer<PlayerStatusUpdatedEvent>> playerStatusUpdatedCallbacks = new ArrayList<>();

    public MatchController() {
        this.clientService = ClientService.getInstance();
        setupRealtimeHandler();
        requestMatches();
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

    public void addSlotChangedCallback(Consumer<SlotChangedEvent> callback) {
        slotChangedCallbacks.add(callback);
    }

    public void addMatchStartedCallback(Consumer<MatchStartedEvent> callback) {
        matchStartedCallbacks.add(callback);
    }

    public void addMatchScoreCallback(Consumer<MatchScoreEvent> callback) {
        matchScoreCallbacks.add(callback);
    }

    public void addMatchCompletedCallback(Consumer<MatchCompletedEvent> callback) {
        matchCompletedCallbacks.add(callback);
    }

    public void addMatchPasswordUpdatedCallback(Consumer<MatchPasswordUpdatedEvent> callback) {
        matchPasswordUpdatedCallbacks.add(callback);
    }

    public void addMatchNameUpdatedCallback(Consumer<MatchNameUpdatedEvent> callback) {
        matchNameUpdatedCallbacks.add(callback);
    }

    public void addMatchBeatmapUpdatedCallback(Consumer<MatchBeatmapUpdatedEvent> callback) {
        matchBeatmapUpdatedCallbacks.add(callback);
    }

    public void addMatchChangingBeatmapUpdatedCallback(Consumer<MatchChangingBeatmapUpdatedEvent> callback) {
        matchChangingBeatmapUpdatedCallbacks.add(callback);
    }

    public void addMatchWinConditionUpdatedCallback(Consumer<MatchWinConditionUpdatedEvent> callback) {
        matchWinConditionUpdatedCallbacks.add(callback);
    }

    public void addPlayerStatusUpdatedCallback(Consumer<PlayerStatusUpdatedEvent> callback) {
        playerStatusUpdatedCallbacks.add(callback);
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

    public void removeSlotChangedCallback(Consumer<SlotChangedEvent> callback) {
        slotChangedCallbacks.remove(callback);
    }

    public void removeMatchStartedCallback(Consumer<MatchStartedEvent> callback) {
        matchStartedCallbacks.remove(callback);
    }

    public void removeMatchScoreCallback(Consumer<MatchScoreEvent> callback) {
        matchScoreCallbacks.remove(callback);
    }

    public void removeMatchCompletedCallback(Consumer<MatchCompletedEvent> callback) {
        matchCompletedCallbacks.remove(callback);
    }

    public void removeMatchPasswordUpdatedCallback(Consumer<MatchPasswordUpdatedEvent> callback) {
        matchPasswordUpdatedCallbacks.remove(callback);
    }

    public void removeMatchNameUpdatedCallback(Consumer<MatchNameUpdatedEvent> callback) {
        matchNameUpdatedCallbacks.remove(callback);
    }

    public void removeMatchBeatmapUpdatedCallback(Consumer<MatchBeatmapUpdatedEvent> callback) {
        matchBeatmapUpdatedCallbacks.remove(callback);
    }

    public void removeMatchChangingBeatmapUpdatedCallback(Consumer<MatchChangingBeatmapUpdatedEvent> callback) {
        matchChangingBeatmapUpdatedCallbacks.remove(callback);
    }

    public void removeMatchWinConditionUpdatedCallback(Consumer<MatchWinConditionUpdatedEvent> callback) {
        matchWinConditionUpdatedCallbacks.remove(callback);
    }

    public void removePlayerStatusUpdatedCallback(Consumer<PlayerStatusUpdatedEvent> callback) {
        playerStatusUpdatedCallbacks.remove(callback);
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

    public CompletableFuture<Result<CreateMatchResponse>> createMatch(String gameName, String password, int maxPlayers, int beatmapId) {
         CreateMatchRequest requestData =  new CreateMatchRequest(gameName, password, maxPlayers, beatmapId);
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

    public CompletableFuture<Result<KickPlayerResponse>> kickPlayerFromMatch(int matchId, int userId) {
        KickPlayerRequest requestData = new KickPlayerRequest(matchId, userId);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.KICK_PLAYER, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((KickPlayerResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<TransferHostResponse>> transferHost(int matchId, int newHostUserId) {
        TransferHostRequest requestData = new TransferHostRequest(matchId, newHostUserId);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.TRANSFER_HOST, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((TransferHostResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<ChangeMatchSlotResponse>> changeMatchSlot(int matchId, int newSlotIndex) {
        ChangeMatchSlotRequest requestData = new ChangeMatchSlotRequest(matchId, newSlotIndex);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.CHANGE_MATCH_SLOT, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((ChangeMatchSlotResponse) result.getValue());
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

    public CompletableFuture<Result<PlayerFinishedEventResponse>> sendPlayerFinishedEvent(PlayerFinishedEvent event) {
        PlayerFinishedEventRequest requestData = new PlayerFinishedEventRequest(event);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.PLAYER_FINISHED_MATCH, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((PlayerFinishedEventResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<UpdateMatchPasswordResponse>> updateMatchPassword(int matchId, String newPassword) {
        UpdateMatchPasswordRequest requestData = new UpdateMatchPasswordRequest(matchId, newPassword);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.UPDATE_MATCH_PASSWORD, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((UpdateMatchPasswordResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<UpdateMatchNameResponse>> updateMatchName(int matchId, String newName) {
        UpdateMatchNameRequest requestData = new UpdateMatchNameRequest(matchId, newName);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.UPDATE_MATCH_NAME, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((UpdateMatchNameResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<UpdateMatchWinConditionResponse>> updateMatchWinCondition(int matchId, MatchWinCondition newWinCondition) {
        UpdateMatchWinConditionRequest requestData = new UpdateMatchWinConditionRequest(matchId, newWinCondition);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.UPDATE_MATCH_WIN_CONDITION, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((UpdateMatchWinConditionResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<UpdatePlayerStatusResponse>> updatePlayerStatus(int matchId, PlayerStatus newStatus) {
        UpdatePlayerStatusRequest requestData = new UpdatePlayerStatusRequest(matchId, newStatus);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.UPDATE_PLAYER_STATUS, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((UpdatePlayerStatusResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<UpdateMatchBeatmapResponse>> updateMatchBeatmap(int matchId, int newBeatmapId) {
        UpdateMatchBeatmapRequest requestData = new UpdateMatchBeatmapRequest(matchId, newBeatmapId);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.UPDATE_MATCH_BEATMAP, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((UpdateMatchBeatmapResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<UpdateMatchChangingBeatmapResponse>> updateMatchChangingBeatmap(int matchId, boolean isChangingBeatmap) {
        UpdateMatchChangingBeatmapRequest requestData = new UpdateMatchChangingBeatmapRequest(matchId, isChangingBeatmap);
        RequestMessage request = new RequestMessage(MessageType.MATCH, MessageAction.UPDATE_MATCH_CHANGING_BEATMAP, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((UpdateMatchChangingBeatmapResponse) result.getValue());
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
        } else if (message.getType() == RealtimeMessageType.SLOT_CHANGED) {
            if (message.getPayload() instanceof SlotChangedEvent) {
                SlotChangedEvent event = (SlotChangedEvent) message.getPayload();
                updatePlayerSlot(event.getMatchId(), event.getUserId(), event.getNewSlotIndex());
                notifySlotChanged(event);
            }
        } else if (message.getType() == RealtimeMessageType.MATCH_PASSWORD_UPDATED) {
            if (message.getPayload() instanceof MatchPasswordUpdatedEvent) {
                MatchPasswordUpdatedEvent event = (MatchPasswordUpdatedEvent) message.getPayload();
                notifyMatchPasswordUpdated(event);
            }
        } else if (message.getType() == RealtimeMessageType.MATCH_NAME_UPDATED) {
            if (message.getPayload() instanceof MatchNameUpdatedEvent) {
                notifyMatchNameUpdated((MatchNameUpdatedEvent) message.getPayload());
            }
        } else if (message.getType() == RealtimeMessageType.MATCH_BEATMAP_UPDATED) {
            if (message.getPayload() instanceof MatchBeatmapUpdatedEvent) {
                MatchBeatmapUpdatedEvent event = (MatchBeatmapUpdatedEvent) message.getPayload();
                updateMatchBeatmapInList(event.getMatchId(), event.getNewBeatmapDto());
                notifyMatchBeatmapUpdated(event);
            }
        } else if (message.getType() == RealtimeMessageType.MATCH_CHANGING_BEATMAP_UPDATED) {
            if (message.getPayload() instanceof MatchChangingBeatmapUpdatedEvent) {
                MatchChangingBeatmapUpdatedEvent event = (MatchChangingBeatmapUpdatedEvent) message.getPayload();
                updateMatchChangingBeatmapInList(event.getMatchId(), event.isChangingBeatmap());
                notifyMatchChangingBeatmapUpdated(event);
            }
        } else if (message.getType() == RealtimeMessageType.MATCH_WIN_CONDITION_UPDATED) {
            if (message.getPayload() instanceof MatchWinConditionUpdatedEvent) {
                notifyMatchWinConditionUpdated((MatchWinConditionUpdatedEvent) message.getPayload());
            }
        } else if (message.getType() == RealtimeMessageType.PLAYER_STATUS_UPDATED) {
            if (message.getPayload() instanceof PlayerStatusUpdatedEvent) {
                PlayerStatusUpdatedEvent event = (PlayerStatusUpdatedEvent) message.getPayload();
                notifyPlayerStatusUpdated(event);
            }
        } else if (message.getType() == RealtimeMessageType.MATCH_COMPLETED) {
            if (message.getPayload() instanceof MatchCompletedEvent) {
                MatchCompletedEvent event = (MatchCompletedEvent) message.getPayload();
                notifyMatchCompleted(event);
            }
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

    private void updatePlayerSlot(int matchId, int userId, int newSlotIndex) {
        for (MatchDto match : matches) {
            if (match.getId() == matchId) {
                for (MatchPlayerDto player : match.getPlayers()) {
                    if (player.getUserId() == userId) {
                        player.setMatchSlotIndex(newSlotIndex);
                        return;
                    }
                }
                System.err.println("Player with ID " + userId + " not found in match " + matchId + " to update slot.");
                return;
            }
        }
        System.err.println("Match with ID " + matchId + " not found to update player slot.");
    }

    private void updateMatchNameInList(int matchId, String newName) {
        for (MatchDto match : matches) {
            if (match.getId() == matchId) {
                match.setName(newName);
                return;
            }
        }
        System.err.println("Match with ID " + matchId + " not found to update name.");
    }

    private void updateMatchBeatmapInList(int matchId, BeatmapDto newBeatmapDto) {
        for (MatchDto match : matches) {
            if (match.getId() == matchId) {
                match.setBeatmap(newBeatmapDto);
                return;
            }
        }
        System.err.println("Match with ID " + matchId + " not found to update beatmap.");
    }

    private void updateMatchChangingBeatmapInList(int matchId, boolean isChangingBeatmap) {
        for (MatchDto match : matches) {
            if (match.getId() == matchId) {
                match.setChangingBeatmap(isChangingBeatmap);
                return;
            }
        }
        System.err.println("Match with ID " + matchId + " not found to update changing beatmap status.");
    }

    private void updateMatchWinConditionInList(int matchId, MatchWinCondition newWinCondition) {
        for (MatchDto match : matches) {
            if (match.getId() == matchId) {
                match.setWinCondition(newWinCondition);
                return;
            }
        }
        System.err.println("Match with ID " + matchId + " not found to update win condition.");
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

    private void notifyMatchCompleted(MatchCompletedEvent event) {
        for (Consumer<MatchCompletedEvent> callback : matchCompletedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in match completed callback: " + e.getMessage());
            }
        }
    }

    private void notifySlotChanged(SlotChangedEvent event) {
        for (Consumer<SlotChangedEvent> callback : slotChangedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in slot changed callback: " + e.getMessage());
            }
        }
    }

    private void notifyMatchPasswordUpdated(MatchPasswordUpdatedEvent event) {
        for (Consumer<MatchPasswordUpdatedEvent> callback : matchPasswordUpdatedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in match password updated callback: " + e.getMessage());
            }
        }
    }

    private void notifyMatchNameUpdated(MatchNameUpdatedEvent event) {
        for (Consumer<MatchNameUpdatedEvent> callback : matchNameUpdatedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in match name updated callback: " + e.getMessage());
            }
        }
    }

    private void notifyMatchBeatmapUpdated(MatchBeatmapUpdatedEvent event) {
        for (Consumer<MatchBeatmapUpdatedEvent> callback : matchBeatmapUpdatedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in match beatmap updated callback: " + e.getMessage());
            }
        }
    }

    private void notifyMatchChangingBeatmapUpdated(MatchChangingBeatmapUpdatedEvent event) {
        for (Consumer<MatchChangingBeatmapUpdatedEvent> callback : matchChangingBeatmapUpdatedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in match changing beatmap updated callback: " + e.getMessage());
            }
        }
    }

    private void notifyMatchWinConditionUpdated(MatchWinConditionUpdatedEvent event) {
        updateMatchWinConditionInList(event.getMatchId(), event.getNewWinCondition());
        for (Consumer<MatchWinConditionUpdatedEvent> callback : matchWinConditionUpdatedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in match win condition updated callback: " + e.getMessage());
            }
        }
    }

    private void notifyPlayerStatusUpdated(PlayerStatusUpdatedEvent event) {
        updatePlayerStatusInList(event.getMatchId(), event.getUserId(), event.getNewStatus());
        for (Consumer<PlayerStatusUpdatedEvent> callback : playerStatusUpdatedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in player status updated callback: " + e.getMessage());
            }
        }
    }

    private void updatePlayerStatusInList(int matchId, int userId, PlayerStatus newStatus) {
        for (MatchDto match : matches) {
            if (match.getId() == matchId) {
                for (MatchPlayerDto player : match.getPlayers()) {
                    if (player.getUserId() == userId) {
                        player.setStatus(newStatus);
                        return;
                    }
                }
                break;
            }
        }
    }
}