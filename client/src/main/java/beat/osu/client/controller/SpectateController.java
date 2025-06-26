package beat.osu.client.controller;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.game.SpectateDto;
import beat.osu.shared.dto.game.events.SpectateEvent;
import beat.osu.shared.dto.game.events.SpectateStatusEvent;
import beat.osu.shared.dto.game.requests.*;
import beat.osu.shared.dto.game.responses.*;
import beat.osu.shared.enums.message.MessageAction;
import beat.osu.shared.enums.message.MessageType;
import beat.osu.shared.enums.message.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;
import beat.osu.shared.models.RequestMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SpectateController {
    private final ClientService clientService;

    private final List<Consumer<SpectateEvent>> spectateEventCallbacks = new ArrayList<>();
    private final List<Consumer<SpectateStatusEvent>> spectateStatusEventCallbacks = new ArrayList<>();
    private final List<Consumer<String>> playerExitedCallbacks = new ArrayList<>();

    public SpectateController() {
        this.clientService = ClientService.getInstance();
        setupRealtimeHandler();
    }

    public void addSpectateEventCallback(Consumer<SpectateEvent> callback) {
        spectateEventCallbacks.add(callback);
    }

    public void removeSpectateEventCallback(Consumer<SpectateEvent> callback) {
        spectateEventCallbacks.remove(callback);
    }

    public void addSpectateStatusEventCallback(Consumer<SpectateStatusEvent> callback) {
        spectateStatusEventCallbacks.add(callback);
    }

    public void removeSpectateStatusEventCallback(Consumer<SpectateStatusEvent> callback) {
        spectateStatusEventCallbacks.remove(callback);
    }

    public void addPlayerExitedCallback(Consumer<String> callback) {
        playerExitedCallbacks.add(callback);
    }

    public void removePlayerExitedCallback(Consumer<String> callback) {
        playerExitedCallbacks.remove(callback);
    }

    // start spectate
    public CompletableFuture<Result<StartSpectateResponse>> startSpectate(SpectateDto spectateDto) {
        StartSpectateRequest requestData = new StartSpectateRequest(spectateDto);
        RequestMessage request = new RequestMessage(MessageType.SPECTATE, MessageAction.START_SPECTATE, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((StartSpectateResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }
    
    // stop spectate
    public CompletableFuture<Result<StopSpectateResponse>> stopSpectate() {
        RequestMessage request = new RequestMessage(MessageType.SPECTATE, MessageAction.STOP_SPECTATE, new StopSpectateRequest("stop spectate"));

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((StopSpectateResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    // notify spectators spectate status change
    public CompletableFuture<Result<NotifySpectateStatusResponse>> notifySpectatorsStatusChange(SpectateStatusEvent event) {
        NotifySpectateStatusRequest requestData = new NotifySpectateStatusRequest(event);
        RequestMessage request = new RequestMessage(MessageType.SPECTATE, MessageAction.CHANGE_SPECTATE_STATUS, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((NotifySpectateStatusResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    // notify spectators player exited game
    public CompletableFuture<Result<NotifyExitResponse>> notifySpectatorsPlayerExited() {
        RequestMessage request = new RequestMessage(MessageType.SPECTATE, MessageAction.PLAYER_EXIT_GAME, new NotifyExitRequest("Player exited game"));

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success(null);
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<SendSpectateEventResponse>> sendSpectateEvent(SpectateEvent event) {
        SendSpectateEventRequest requestData = new SendSpectateEventRequest(event);
        RequestMessage request = new RequestMessage(MessageType.SPECTATE, MessageAction.SEND_SPECTATE_EVENT, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((SendSpectateEventResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    private void setupRealtimeHandler() {
        if (clientService.getConnection() != null && clientService.getConnection().getRealtimeHandler() != null) {
            clientService.getConnection().addRealtimeMessageCallback(this::handleRealtimeMessage);
        }
    }

    private void handleRealtimeMessage(RealtimeMessage message) {
        if (message.getType() == RealtimeMessageType.SPECTATE_EVENT) {
            if (message.getPayload() instanceof SpectateEvent) {
                SpectateEvent event = (SpectateEvent) message.getPayload();
                notifySpectateEvent(event);
            }
        } else if (message.getType() == RealtimeMessageType.SPECTATE_STATUS_CHANGE) {
            if (message.getPayload() instanceof SpectateStatusEvent) {
                SpectateStatusEvent event = (SpectateStatusEvent) message.getPayload();
                notifySpectateStatusEvent(event);
            }
        } else if (message.getType() == RealtimeMessageType.PLAYER_EXIT_GAME) {
            if (message.getPayload() instanceof String) {
                String exitMessage = (String) message.getPayload();
                notifyPlayerExited(exitMessage);
            }
        }
    }

    private void notifySpectateEvent(SpectateEvent event) {
        for (Consumer<SpectateEvent> callback : spectateEventCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in spectate event callback: " + e.getMessage());
            }
        }
    }

    private void notifySpectateStatusEvent(SpectateStatusEvent event) {
        for (Consumer<SpectateStatusEvent> callback : spectateStatusEventCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in spectate status event callback: " + e.getMessage());
            }
        }
    }

    private void notifyPlayerExited(String message) {
        for (Consumer<String> callback : playerExitedCallbacks) {
            try {
                callback.accept(message);
            } catch (Exception e) {
                System.err.println("Error in player exited callback: " + e.getMessage());
            }
        }
    }
}
