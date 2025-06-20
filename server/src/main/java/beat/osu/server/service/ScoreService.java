package beat.osu.server.service;

import beat.osu.server.repositories.ScoreRepository;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.score.requests.InsertScoreRequest;
import beat.osu.shared.dto.score.responses.InsertScoreResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ScoreService {

    private ScoreRepository scoreRepository;

    public Result<InsertScoreResponse> insertScore(InsertScoreRequest request) {
        if (request == null) {
            return Result.failure(Error.badRequest("Score data is missing"));
        }

        try {
            scoreRepository.insertScore(
                    1,
                    request.getBeatmapId(),
                    request.getUserId(),
                    request.getScore(),
                    request.getHighestCombo(),
                    request.getAccuracy(),
                    request.getPerfectHit(),
                    request.getGekiHit(),
                    request.getGreatHit(),
                    request.getKatuHit(),
                    request.getGoodHit(),
                    request.getMiss(),
                    request.getGrade(),
                    request.getDate()
            );

            System.out.println("Score inserted successfully for user ID: " + request.getUserId());
            String message = "Score inserted successfully for user ID: " + request.getUserId() + " on beatmap ID: " + request.getBeatmapId();
            return Result.success(new InsertScoreResponse(message));
        } catch (RuntimeException e) {
            return Result.failure(Error.internal("Failed to insert score: " + e.getMessage()));
        }
    }
}
