package beat.osu.server.service;

import beat.osu.server.entities.Score;
import beat.osu.server.entities.User;
import beat.osu.server.repositories.ScoreRepository;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.score.ScoreDto;
import beat.osu.shared.dto.score.requests.GetScoreRequest;
import beat.osu.shared.dto.score.requests.InsertScoreRequest;
import beat.osu.shared.dto.score.responses.GetAllScoresResponse;
import beat.osu.shared.dto.score.responses.InsertScoreResponse;
import lombok.AllArgsConstructor;

import java.util.ArrayList;

@AllArgsConstructor
public class ScoreService {

    private ScoreRepository scoreRepository;
    private UserService userService;

    public Result<GetAllScoresResponse> getScoresByBeatmapId(GetScoreRequest request) {
        if (request == null) {
            return Result.failure(Error.badRequest("Score request data is missing"));
        }

        try {
            ArrayList<Score> scores = scoreRepository.getScoresByBeatmapId(request.getBeatmapId());
            ArrayList<ScoreDto> scoreDtos = new ArrayList<>();
            for (Score score : scores) {
                User user = userService.findUserById(score.getUserId());
                ScoreDto scoreDto = new ScoreDto(
                        score.getId(),
                        score.getBeatmapId(),
                        score.getUserId(),
                        score.getScore(),
                        score.getHighestCombo(),
                        score.getAccuracy(),
                        score.getPerfectHit(),
                        score.getGekiHit(),
                        score.getGreatHit(),
                        score.getKatuHit(),
                        score.getGoodHit(),
                        score.getMiss(),
                        score.getGrade(),
                        score.getDate(),
                        user.getUsername(),
                        user.getProfilePicture());
                scoreDtos.add(scoreDto);
            }

            return Result.success(new GetAllScoresResponse(scoreDtos));
        } catch (RuntimeException e) {
            return Result.failure(Error.internal("Failed to retrieve scores: " + e.getMessage()));
        }
    }

    public Result<InsertScoreResponse> insertScore(InsertScoreRequest request) {
        if (request == null) {
            return Result.failure(Error.badRequest("Score data is missing"));
        }

        try {
            scoreRepository.insertScore(
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
                    request.getDate());

            String message = "Score inserted successfully for user ID: " + request.getUserId() + " on beatmap ID: "
                    + request.getBeatmapId();
            return Result.success(new InsertScoreResponse(message));
        } catch (RuntimeException e) {
            return Result.failure(Error.internal("Failed to insert score: " + e.getMessage()));
        }
    }
}
