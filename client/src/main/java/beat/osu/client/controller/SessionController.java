package beat.osu.client.controller;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.session.requests.CreateSessionDataRequest;
import beat.osu.shared.dto.session.requests.RemoveSessionDataRequest;
import beat.osu.shared.dto.session.responses.CreateSessionDataResponse;
import beat.osu.shared.dto.session.responses.RemoveSessionDataResponse;
import beat.osu.shared.enums.MessageAction;
import beat.osu.shared.enums.MessageType;
import beat.osu.shared.models.RequestMessage;

import java.util.concurrent.CompletableFuture;

public class SessionController {
    private final ClientService clientService;

    public SessionController() {
        this.clientService = ClientService.getInstance();
    }

    public CompletableFuture<Result<CreateSessionDataResponse>> createPlayingBeatmapSession(int userId, Object value) {
        CreateSessionDataRequest requestData = new CreateSessionDataRequest(userId, "playingBeatmap", value);

        RequestMessage request = new RequestMessage(MessageType.SESSION, MessageAction.CREATE_SESSION_DATA, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    System.out.println("result success " + result.getValue());
                    return Result.success((CreateSessionDataResponse) result.getValue());
                } else {
                    System.out.println("result failure " + result.getError().getMessage());
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<RemoveSessionDataResponse>> removePlayingBeatmapSession(int userId) {
        RemoveSessionDataRequest requestData = new RemoveSessionDataRequest(userId, "playingBeatmap");

        RequestMessage request = new RequestMessage(MessageType.SESSION, MessageAction.REMOVE_SESSION_DATA, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    System.out.println("result success " + result.getValue());
                    return Result.success((RemoveSessionDataResponse) result.getValue());
                } else {
                    System.out.println("result failure " + result.getError().getMessage());
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }
}
