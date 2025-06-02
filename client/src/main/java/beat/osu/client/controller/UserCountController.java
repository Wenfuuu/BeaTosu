package beat.osu.client.controller;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.system.responses.GetCurrentUserCountResponse;
import beat.osu.shared.enums.MessageAction;
import beat.osu.shared.enums.MessageType;
import beat.osu.shared.enums.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;
import beat.osu.shared.models.RequestMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UserCountController {
    private final ClientService clientService;
    private final List<Consumer<Integer>> userCountCallbacks = new ArrayList<>();
    private Integer currentUserCount = 0;

    public UserCountController() {
        this.clientService = ClientService.getInstance();
        requestCurrentUserCount();
        setupRealtimeHandler();
    }

    public void addUserCountCallback(Consumer<Integer> callback) {
        userCountCallbacks.add(callback);
    }
    
    public void removeUserCountCallback(Consumer<Integer> callback) {
        userCountCallbacks.remove(callback);
    }
    
    private void setupRealtimeHandler() {
        if (clientService.getConnection() != null && clientService.getConnection().getRealtimeHandler() != null) {
            clientService.getConnection().getRealtimeHandler().addCallback(this::handleRealtimeMessage);
        }
    }
    
    private void requestCurrentUserCount() {
        if (clientService.getConnection() != null && clientService.getConnection().isConnected()) {
            RequestMessage request = new RequestMessage(MessageType.SYSTEM, MessageAction.GET_USER_COUNT, null);
            
            clientService.getConnection().sendRequest(request).thenAccept(response -> {
                try {
                    Result<?> result = (Result<?>) response;
                    if (result.isSuccess()) {
                        GetCurrentUserCountResponse userCountResponse = (GetCurrentUserCountResponse) result.getValue();
                        currentUserCount = userCountResponse.getUserCount();
                        notifyUserCountCallbacks();
                    }
                } catch (Exception e) {
                    System.err.println("Error processing user count response: " + e.getMessage());
                }
            }).exceptionally(throwable -> {
                System.err.println("Error requesting user count: " + throwable.getMessage());
                return null;
            });
        }
    }
    
    private void handleRealtimeMessage(RealtimeMessage message) {
        if (message.getType() == RealtimeMessageType.USER_COUNT_UPDATE) {
            if (message.getPayload() instanceof Integer) {
                currentUserCount = (Integer) message.getPayload();
                notifyUserCountCallbacks();
            }
        }
    }
    
    private void notifyUserCountCallbacks() {
        for (Consumer<Integer> callback : userCountCallbacks) {
            try {
                callback.accept(currentUserCount);
            } catch (Exception e) {
                System.err.println("Error in user count callback: " + e.getMessage());
            }
        }
    }
}
