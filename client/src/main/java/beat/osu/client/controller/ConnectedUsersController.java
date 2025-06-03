package beat.osu.client.controller;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.system.responses.GetConnectedUsersResponse;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.enums.MessageAction;
import beat.osu.shared.enums.MessageType;
import beat.osu.shared.enums.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;
import beat.osu.shared.models.RequestMessage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConnectedUsersController {
    private final ClientService clientService;

    @Getter
    private List<UserDto> connectedUsers = new ArrayList<>();
    private final List<Consumer<UserDto>> userJoinedCallbacks = new ArrayList<>();
    private final List<Consumer<UserDto>> userLeftCallbacks = new ArrayList<>();
    private final List<Consumer<Integer>> userCountCallbacks = new ArrayList<>();

    public ConnectedUsersController() {
        this.clientService = ClientService.getInstance();
        requestConnectedUsers();
        setupRealtimeHandler();
    }

    public void addUserJoinedCallback(Consumer<UserDto> callback) {
        userJoinedCallbacks.add(callback);
    }

    public void addUserLeftCallback(Consumer<UserDto> callback) {
        userLeftCallbacks.add(callback);
    }

    public void addUserCountCallback(Consumer<Integer> callback) {
        userCountCallbacks.add(callback);
    }

    public void removeUserJoinedCallback(Consumer<UserDto> callback) {
        userJoinedCallbacks.remove(callback);
    }

    public void removeUserLeftCallback(Consumer<UserDto> callback) {
        userLeftCallbacks.remove(callback);
    }

    public void removeUserCountCallback(Consumer<Integer> callback) {
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
        if (message.getType() == RealtimeMessageType.ADD_CONNECTED_USER) {
            if (message.getPayload() instanceof UserDto) {
                UserDto user = (UserDto) message.getPayload();
                connectedUsers.add(user);

                notifyUserJoined(user);
                notifyUserCountChanged();
            }
        } else if (message.getType() == RealtimeMessageType.REMOVE_CONNECTED_USER) {
            if (message.getPayload() instanceof UserDto) {
                UserDto user = (UserDto) message.getPayload();
                connectedUsers.removeIf(u -> u.getId() == user.getId());

                notifyUserLeft(user);
                notifyUserCountChanged();
            }
        }
    }

    private void notifyUserJoined(UserDto user) {
        for (Consumer<UserDto> callback : userJoinedCallbacks) {
            try {
                callback.accept(user);
            } catch (Exception e) {
                System.err.println("Error in user joined callback: " + e.getMessage());
            }
        }
    }

    private void notifyUserLeft(UserDto user) {
        for (Consumer<UserDto> callback : userLeftCallbacks) {
            try {
                callback.accept(user);
            } catch (Exception e) {
                System.err.println("Error in user left callback: " + e.getMessage());
            }
        }
    }
    
    private void notifyUserCountChanged() {
        for (Consumer<Integer> callback : userCountCallbacks) {
            try {
                callback.accept(connectedUsers.size());
            } catch (Exception e) {
                System.err.println("Error in user count callback: " + e.getMessage());
            }
        }
    }
}
