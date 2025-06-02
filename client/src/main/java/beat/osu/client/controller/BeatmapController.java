package beat.osu.client.controller;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapRequest;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapSetRequest;
import beat.osu.shared.dto.beatmap.responses.GetAllBeatmapsResponse;
import beat.osu.shared.dto.beatmap.responses.InsertBeatmapResponse;
import beat.osu.shared.dto.beatmap.responses.InsertBeatmapSetResponse;
import beat.osu.shared.enums.MessageAction;
import beat.osu.shared.enums.MessageType;
import beat.osu.shared.models.RequestMessage;

import java.util.concurrent.CompletableFuture;

public class BeatmapController {
    private final ClientService clientService;

    public BeatmapController() {
        this.clientService = ClientService.getInstance();
    }

    public CompletableFuture<Result<GetAllBeatmapsResponse>> getAllBeatmaps() {
        RequestMessage request = new RequestMessage(MessageType.BEATMAP,
                MessageAction.GET_ALL_BEATMAPS, null);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

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

    public CompletableFuture<Result<InsertBeatmapSetResponse>> insertBeatmapSet (
            int id,
            String title,
            String artist,
            String creator,
            String length,
            int bpm
    ) {
        InsertBeatmapSetRequest requestData = new InsertBeatmapSetRequest(id, title, artist, creator, length, bpm);

        RequestMessage request = new RequestMessage(MessageType.BEATMAP,
                MessageAction.INSERT_BEATMAP_SET, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((InsertBeatmapSetResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }

    public CompletableFuture<Result<InsertBeatmapResponse>> insertBeatmap (
            int id,
            int beatmapSetId,
            String version,
            double hpDrainRate,
            double circleSize,
            double overallDifficulty,
            double approachRate,
            double slideMultiplier,
            double sliderTickRate,
            double starRating
    ) {
        InsertBeatmapRequest requestData = new InsertBeatmapRequest(id, beatmapSetId, version, hpDrainRate, circleSize,
                overallDifficulty, approachRate, slideMultiplier, sliderTickRate, starRating);

        RequestMessage request = new RequestMessage(MessageType.BEATMAP,
                MessageAction.INSERT_BEATMAP, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((InsertBeatmapResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }
}
