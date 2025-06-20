package beat.osu.client.controller;

import beat.osu.client.service.ClientService;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.responses.InsertBeatmapSetResponse;
import beat.osu.shared.dto.score.requests.InsertScoreRequest;
import beat.osu.shared.dto.score.responses.InsertScoreResponse;
import beat.osu.shared.enums.MessageAction;
import beat.osu.shared.enums.MessageType;
import beat.osu.shared.models.RequestMessage;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public class ScoreController {
    private final ClientService clientService;

    public ScoreController() {
        this.clientService = ClientService.getInstance();
    }

    public CompletableFuture<Result<InsertScoreResponse>> insertScore(
            int id,
            int beatmapId,
            int userId,
            int score,
            int highestCombo,
            double accuracy,
            int perfectHit,
            int gekiHit,
            int greatHit,
            int katuHit,
            int goodHit,
            int miss,
            String grade,
            LocalDateTime date
    ) {
        InsertScoreRequest requestData = new InsertScoreRequest(id, beatmapId, userId, score, highestCombo, accuracy,
                perfectHit, gekiHit, greatHit, katuHit, goodHit, miss, grade, date);

        RequestMessage request = new RequestMessage(MessageType.SCORE, MessageAction.INSERT_SCORE, requestData);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Object response = clientService.getConnection().sendRequest(request).get();

                Result<?> result = (Result<?>) response;
                if (result.isSuccess()) {
                    return Result.success((InsertScoreResponse) result.getValue());
                } else {
                    return Result.failure(result.getError());
                }
            } catch (Exception e) {
                return Result.failure(Error.network(e.getMessage()));
            }
        });
    }
}
