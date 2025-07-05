package beat.osu.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.dto.user.events.UserUpdatedEvent;
import beat.osu.shared.dto.user.requests.GetUsernameByIdRequest;
import beat.osu.shared.dto.user.requests.UpdateUserRequest;
import beat.osu.shared.dto.user.responses.GetUsernameByIdResponse;
import beat.osu.shared.dto.user.responses.UpdateUserResponse;
import beat.osu.shared.enums.message.MessageAction;
import beat.osu.shared.enums.message.MessageType;
import beat.osu.shared.enums.message.RealtimeMessageType;
import beat.osu.shared.models.RealtimeMessage;
import beat.osu.shared.models.RequestMessage;

public class UserController {
    private final ClientService clientService;

    private final List<Consumer<UserUpdatedEvent>> userUpdatedCallbacks = new ArrayList<>();

    public UserController() {
        this.clientService = ClientService.getInstance();
        setupRealtimeHandler();
    }

    public void addUserUpdatedCallback(Consumer<UserUpdatedEvent> callback) {
        userUpdatedCallbacks.add(callback);
    }

    public void removeUserUpdatedCallback(Consumer<UserUpdatedEvent> callback) {
        userUpdatedCallbacks.remove(callback);
    }

    private void setupRealtimeHandler() {
        if (clientService.getConnection() != null && clientService.getConnection().getRealtimeHandler() != null) {
            clientService.getConnection().getRealtimeHandler().addCallback(this::handleRealtimeMessage);
        }
    }

    private void handleRealtimeMessage(RealtimeMessage message) {
        if (message.getType() == RealtimeMessageType.USER_UPDATED) {
            if (message.getPayload() instanceof UserUpdatedEvent) {
                UserUpdatedEvent event = (UserUpdatedEvent) message.getPayload();
                notifyUserUpdated(event);
            }
        }
    }

    private void notifyUserUpdated(UserUpdatedEvent event) {
        for (Consumer<UserUpdatedEvent> callback : userUpdatedCallbacks) {
            try {
                callback.accept(event);
            } catch (Exception e) {
                System.err.println("Error in user updated callback: " + e.getMessage());
            }
        }
    }

    public CompletableFuture<Result<UpdateUserResponse>> updateUser(UserDto userDto) {
        UpdateUserRequest requestData = new UpdateUserRequest(userDto);
        RequestMessage request = new RequestMessage(
                MessageType.USER,
                MessageAction.UPDATE_USER,
                requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();
                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((UpdateUserResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<GetUsernameByIdResponse>> getUsernameById(int userId) {
        GetUsernameByIdRequest requestData = new GetUsernameByIdRequest(userId);
        RequestMessage request = new RequestMessage(
                MessageType.USER,
                MessageAction.GET_USERNAME_BY_ID,
                requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();
                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((GetUsernameByIdResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }
}
