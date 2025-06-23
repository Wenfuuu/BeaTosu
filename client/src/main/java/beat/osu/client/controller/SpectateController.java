package beat.osu.client.controller;

import beat.osu.client.service.ClientService;
import beat.osu.shared.dto.game.events.ReplayEvent;
import beat.osu.shared.enums.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpectateController {
    private final ClientService clientService;

    private final List<Consumer<ReplayEvent>> replayEventCallbacks = new ArrayList<>();

    public SpectateController() {
        this.clientService = ClientService.getInstance();
        setupRealtimeHandler();
    }

    public void addReplayEventCallback(Consumer<ReplayEvent> callback) {
        replayEventCallbacks.add(callback);
    }

    public void removeReplayEventCallback(Consumer<ReplayEvent> callback) {
        replayEventCallbacks.remove(callback);
    }

    private void setupRealtimeHandler() {
        if (clientService.getConnection() != null && clientService.getConnection().getRealtimeHandler() != null) {
            clientService.getConnection().addRealtimeMessageCallback(this::handleRealtimeMessage);
        }
    }

    private void handleRealtimeMessage(RealtimeMessage message) {
        if (message.getType() == RealtimeMessageType.REPLAY_EVENT) {
            if (message.getPayload() instanceof ReplayEvent) {
                ReplayEvent event = (ReplayEvent) message.getPayload();
                notifyReplayEvent(event);
            }
        }
    }

    private void notifyReplayEvent(ReplayEvent event) {
        for (Consumer<ReplayEvent> callback : replayEventCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in replay event callback: " + e.getMessage());
            }
        }
    }
}
