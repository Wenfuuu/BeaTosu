package beat.osu.client.controller;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.game.SpectateDto;
import beat.osu.shared.dto.game.events.SpectateEvent;
import beat.osu.shared.dto.game.requests.SendSpectateEventRequest;
import beat.osu.shared.dto.game.requests.StartSpectateRequest;
import beat.osu.shared.dto.game.responses.SendSpectateEventResponse;
import beat.osu.shared.dto.game.responses.StartSpectateResponse;
import beat.osu.shared.dto.game.responses.StopSpectateResponse;
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

    private final List<Consumer<SpectateEvent>> replayEventCallbacks = new ArrayList<>();

    public SpectateController() {
        this.clientService = ClientService.getInstance();
        setupRealtimeHandler();
    }

    public void addSpectateEventCallback(Consumer<SpectateEvent> callback) {
        replayEventCallbacks.add(callback);
    }

    public void removeSpectateEventCallback(Consumer<SpectateEvent> callback) {
        replayEventCallbacks.remove(callback);
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
        RequestMessage request = new RequestMessage(MessageType.SPECTATE, MessageAction.STOP_SPECTATE, null);

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
                notifyReplayEvent(event);
            }
        }
    }

    private void notifyReplayEvent(SpectateEvent event) {
        for (Consumer<SpectateEvent> callback : replayEventCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in replay event callback: " + e.getMessage());
            }
        }
    }
}
