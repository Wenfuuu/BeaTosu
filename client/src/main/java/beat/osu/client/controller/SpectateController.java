package beat.osu.client.controller;

import beat.osu.client.service.ClientService;
import beat.osu.shared.dto.game.events.SpectateEvent;
import beat.osu.shared.enums.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;

import java.util.ArrayList;
import java.util.List;
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
