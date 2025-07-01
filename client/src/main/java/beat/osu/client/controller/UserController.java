package beat.osu.client.controller;

import java.util.concurrent.CompletableFuture;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.user.requests.GetUsernameByIdRequest;
import beat.osu.shared.dto.user.responses.GetUsernameByIdResponse;
import beat.osu.shared.enums.message.MessageAction;
import beat.osu.shared.enums.message.MessageType;
import beat.osu.shared.models.RequestMessage;

public class UserController {
    private final ClientService clientService;

    public UserController() {
        this.clientService = ClientService.getInstance();
    }

    public CompletableFuture<Result<GetUsernameByIdResponse>> getUsernameById(int userId) {
        GetUsernameByIdRequest requestData = new GetUsernameByIdRequest(userId);
        RequestMessage request = new RequestMessage(
                MessageType.USER,
                MessageAction.GET_USERNAME_BY_ID,
                requestData
        );

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
