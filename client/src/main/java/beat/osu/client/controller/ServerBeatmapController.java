package beat.osu.client.controller;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.responses.GetAllBeatmapsResponse;
import beat.osu.shared.enums.MessageAction;
import beat.osu.shared.enums.MessageType;
import beat.osu.shared.models.Message;

import java.util.concurrent.CompletableFuture;

public class ServerBeatmapController {
    private final ClientService clientService;

    public ServerBeatmapController() {
        this.clientService = ClientService.getInstance();
    }

    public CompletableFuture<Result<GetAllBeatmapsResponse>> getAllBeatmaps() {
        Message getAllBeatmapsMessage = new Message(MessageType.BEATMAP,
                MessageAction.GET_ALL_BEATMAPS, null, System.currentTimeMillis());

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendMessage(getAllBeatmapsMessage).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((GetAllBeatmapsResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }
}
