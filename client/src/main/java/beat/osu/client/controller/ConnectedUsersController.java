package beat.osu.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.system.responses.GetConnectedUsersResponse;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.dto.user.events.UserConnectedEvent;
import beat.osu.shared.dto.user.events.UserCountChangedEvent;
import beat.osu.shared.dto.user.events.UserDisconnectedEvent;
import beat.osu.shared.enums.message.MessageAction;
import beat.osu.shared.enums.message.MessageType;
import beat.osu.shared.enums.message.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;
import beat.osu.shared.models.RequestMessage;
import lombok.Getter;

public class ConnectedUsersController {
    private final ClientService clientService;
    @Getter
    private List<UserDto> connectedUsers = new ArrayList<>();

    private final List<Consumer<UserConnectedEvent>> userConnectedCallbacks = new ArrayList<>();
    private final List<Consumer<UserDisconnectedEvent>> userDisconnectedCallbacks = new ArrayList<>();
    private final List<Consumer<UserCountChangedEvent>> userCountCallbacks = new ArrayList<>();

    public ConnectedUsersController() {
        this.clientService = ClientService.getInstance();
        requestConnectedUsers();
        setupRealtimeHandler();
    }

    public void addUserConnectedCallback(Consumer<UserConnectedEvent> callback) {
        userConnectedCallbacks.add(callback);
    }

    public void addUserDisconnectedCallback(Consumer<UserDisconnectedEvent> callback) {
        userDisconnectedCallbacks.add(callback);
    }

    public void addUserCountChangedCallback(Consumer<UserCountChangedEvent> callback) {
        userCountCallbacks.add(callback);
    }

    public void removeUserConnectedCallback(Consumer<UserConnectedEvent> callback) {
        userConnectedCallbacks.remove(callback);
    }

    public void removeUserDisconnectedCallback(Consumer<UserDisconnectedEvent> callback) {
        userDisconnectedCallbacks.remove(callback);
    }

    public void removeUserCountCallback(Consumer<UserCountChangedEvent> callback) {
        userCountCallbacks.remove(callback);
    }
    
    private void setupRealtimeHandler() {
        if (clientService.getConnection() != null && clientService.getConnection().getRealtimeHandler() != null) {
            clientService.getConnection().getRealtimeHandler().addCallback(this::handleRealtimeMessage);
        }
    }
    
    private void requestConnectedUsers() {
        if (clientService.getConnection() != null && clientService.getConnection().isConnected()) {
            RequestMessage request = new RequestMessage(MessageType.SYSTEM, MessageAction.GET_CONNECTED_USERS, null);
            
            clientService.getConnection().sendRequest(request).thenAccept(response -> {
                try {
                    Result<?> result = (Result<?>) response;
                    if (result.isSuccess()) {
                        GetConnectedUsersResponse connectedUsersResponse = (GetConnectedUsersResponse) result.getValue();
                        connectedUsers = connectedUsersResponse.getConnectedUsers();
                        notifyUserCountChanged();
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
        if (message.getType() == RealtimeMessageType.USER_CONNECTED) {
            if (message.getPayload() instanceof UserConnectedEvent) {
                UserConnectedEvent event = (UserConnectedEvent) message.getPayload();
                connectedUsers.add(event.getUserDto());
                notifyUserJoined(event);
                notifyUserCountChanged();
            }
        } else if (message.getType() == RealtimeMessageType.USER_DISCONNECTED) {
            if (message.getPayload() instanceof UserDisconnectedEvent) {
                UserDisconnectedEvent event = (UserDisconnectedEvent) message.getPayload();
                connectedUsers.removeIf(u -> u.getId() == event.getUserDto().getId());
                notifyUserLeft(event);
                notifyUserCountChanged();
            }
        }
    }

    private void notifyUserJoined(UserConnectedEvent event) {
        for (Consumer<UserConnectedEvent> callback : userConnectedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in user joined callback: " + e.getMessage());
            }
        }
    }

    private void notifyUserLeft(UserDisconnectedEvent event) {
        for (Consumer<UserDisconnectedEvent> callback : userDisconnectedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in user left callback: " + e.getMessage());
            }
        }
    }
    
    private void notifyUserCountChanged() {
        for (Consumer<UserCountChangedEvent> callback : userCountCallbacks) {
            try {
                callback.accept(new UserCountChangedEvent(connectedUsers.size()));
            } catch (Exception e) {
                System.err.println("Error in user count callback: " + e.getMessage());
            }
        }
    }
}
