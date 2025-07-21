package beat.osu.server.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import beat.osu.server.entities.Beatmap;
import beat.osu.server.entities.BeatmapSet;
import beat.osu.server.repositories.BeatmapRepository;
import beat.osu.server.repositories.BeatmapSetRepository;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.BeatmapDto;
import beat.osu.shared.dto.beatmap.BeatmapSetDto;
import beat.osu.shared.dto.beatmap.requests.GetBeatmapByIdRequest;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapRequest;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapSetRequest;
import beat.osu.shared.dto.beatmap.responses.GetAllBeatmapsResponse;
import beat.osu.shared.dto.beatmap.responses.GetBeatmapByIdResponse;
import beat.osu.shared.dto.beatmap.responses.InsertBeatmapResponse;
import beat.osu.shared.dto.beatmap.responses.InsertBeatmapSetResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BeatmapService {

    private BeatmapSetRepository beatmapSetRepository;
    private BeatmapRepository beatmapRepository;

    public Result<GetAllBeatmapsResponse> getAllBeatmaps() {
        try {
            ArrayList<Beatmap> beatmaps = beatmapRepository.getAllBeatmaps();
            ArrayList<BeatmapSet> beatmapSets = beatmapSetRepository.getAllBeatmapSets();

            Map<Integer, BeatmapSet> setMap;
            try {
                setMap = beatmapSets.stream()
                        .collect(Collectors.toMap(BeatmapSet::getId, set -> set));
            } catch (IllegalStateException e) {
                return Result.failure(Error.internal("Duplicate beatmap set IDs found"));
            }

            ArrayList<BeatmapDto> beatmapDtos = new ArrayList<>();

            for (Beatmap beatmap : beatmaps) {
                BeatmapSet beatmapSet = setMap.get(beatmap.getBeatmapSetId());
                if (beatmapSet != null) {
                    BeatmapSetDto beatmapSetDto = new BeatmapSetDto(
                            beatmapSet.getId(),
                            beatmapSet.getTitle(),
                            beatmapSet.getArtist(),
                            beatmapSet.getCreator(),
                            beatmapSet.getLength(),
                            beatmapSet.getBpm());

                    BeatmapDto beatmapDto = new BeatmapDto(
                            beatmap.getId(),
                            beatmap.getBeatmapSetId(),
                            beatmap.getVersion(),
                            beatmap.getHpDrainRate(),
                            beatmap.getCircleSize(),
                            beatmap.getOverallDifficulty(),
                            beatmap.getApproachRate(),
                            beatmap.getSliderMultiplier(),
                            beatmap.getSliderTickRate(),
                            beatmap.getStarRating(),
                            beatmapSetDto);
                    beatmapDtos.add(beatmapDto);
                } else {
                    return Result.failure(Error.notFound("Beatmap set not found for beatmap ID: " + beatmap.getId()));
                }
            }

            return Result.success(new GetAllBeatmapsResponse(beatmapDtos));
        } catch (RuntimeException e) {
            return Result.failure(Error.internal("Database error: " + e.getMessage()));
        }
    }

    public Result<InsertBeatmapSetResponse> insertBeatmapSet(InsertBeatmapSetRequest request) {
        if (request == null) {
            return Result.failure(Error.badRequest("Beatmap set data is missing"));
        }

        try {
            beatmapSetRepository.insertBeatmapSet(
                    request.getId(),
                    request.getTitle(),
                    request.getArtist(),
                    request.getCreator(),
                    request.getLength(),
                    request.getBpm());

            String message = "Beatmap set inserted successfully with ID: " + request.getId();
            return Result.success(new InsertBeatmapSetResponse(message));
        } catch (SQLException e) {
            if (e.getMessage().startsWith("DUPLICATE_BEATMAP_SET:")) {
                String beatmapSetId = e.getMessage().substring("DUPLICATE_BEATMAP_SET:".length());
                return Result.failure(Error.badRequest("Beatmap set " + beatmapSetId + " already exists"));
            }
            return Result.failure(Error.internal("Database error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return Result.failure(Error.internal("Database error: " + e.getMessage()));
        }
    }

    public Result<InsertBeatmapResponse> insertBeatmap(InsertBeatmapRequest request) {
        try {
            if (request == null) {
                return Result.failure(Error.badRequest("Beatmap data is missing"));
            }

            BeatmapSet beatmapSet = beatmapSetRepository.getBeatmapSetById(request.getBeatmapSetId());

            if (beatmapSet == null) {
                return Result.failure(Error.notFound("Beatmap set not found for ID: " + request.getBeatmapSetId()));
            }

            beatmapRepository.insertBeatmap(
                    request.getId(),
                    request.getBeatmapSetId(),
                    request.getVersion(),
                    request.getHpDrainRate(),
                    request.getCircleSize(),
                    request.getOverallDifficulty(),
                    request.getApproachRate(),
                    request.getSliderMultiplier(),
                    request.getSliderTickRate(),
                    request.getStarRating());

            String message = "Beatmap inserted successfully with ID: " + request.getId();
            return Result.success(new InsertBeatmapResponse(message));
        } catch (SQLException e) {
            if (e.getMessage().startsWith("DUPLICATE_BEATMAP:")) {
                String beatmapId = e.getMessage().substring("DUPLICATE_BEATMAP:".length());
                return Result.failure(Error.badRequest("Beatmap " + beatmapId + " already exists"));
            }
            return Result.failure(Error.internal("Database error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return Result.failure(Error.internal("Database error: " + e.getMessage()));
        }
    }

    public Result<GetBeatmapByIdResponse> getBeatmapById(GetBeatmapByIdRequest request) {
        if (request == null) {
            return Result.failure(Error.badRequest("Beatmap ID request data is missing"));
        }

        try {
            Beatmap beatmap = beatmapRepository.getBeatmapById(request.getId());

            if (beatmap == null) {
                return Result.failure(Error.notFound("Beatmap not found for ID: " + request.getId()));
            }

            BeatmapSet beatmapSet = beatmapSetRepository.getBeatmapSetById(beatmap.getBeatmapSetId());

            if (beatmapSet == null) {
                return Result.failure(Error.notFound("Beatmap set not found for ID: " + beatmap.getBeatmapSetId()));
            }

            BeatmapSetDto beatmapSetDto = new BeatmapSetDto(
                    beatmapSet.getId(),
                    beatmapSet.getTitle(),
                    beatmapSet.getArtist(),
                    beatmapSet.getCreator(),
                    beatmapSet.getLength(),
                    beatmapSet.getBpm());

            BeatmapDto beatmapDto = new BeatmapDto(
                    beatmap.getId(),
                    beatmap.getBeatmapSetId(),
                    beatmap.getVersion(),
                    beatmap.getHpDrainRate(),
                    beatmap.getCircleSize(),
                    beatmap.getOverallDifficulty(),
                    beatmap.getApproachRate(),
                    beatmap.getSliderMultiplier(),
                    beatmap.getSliderTickRate(),
                    beatmap.getStarRating(),
                    beatmapSetDto);

            return Result.success(new GetBeatmapByIdResponse(beatmapDto));
        } catch (RuntimeException e) {
            return Result.failure(Error.internal("Database error: " + e.getMessage()));
        }
    }

    public BeatmapSet getBeatmapSetById(int beatmapId) {
        return beatmapSetRepository.getBeatmapSetById(beatmapId);
    }
}
