package beat.osu.server.service;

import beat.osu.server.entities.Beatmap;
import beat.osu.server.entities.BeatmapSet;
import beat.osu.server.repositories.BeatmapRepository;
import beat.osu.server.repositories.BeatmapSetRepository;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.BeatmapDto;
import beat.osu.shared.dto.beatmap.BeatmapSetDto;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapRequest;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapSetRequest;
import beat.osu.shared.dto.beatmap.responses.GetAllBeatmapsResponse;
import beat.osu.shared.dto.beatmap.responses.InsertBeatmapSetResponse;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

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
                            beatmapSet.getBpm()
                    );

                    BeatmapDto beatmapDto = new BeatmapDto(
                            beatmap.getId(),
                            beatmap.getBeatmapSetId(),
                            beatmap.getVersion(),
                            beatmap.getHpDrainRate(),
                            beatmap.getCircleSize(),
                            beatmap.getOverallDifficulty(),
                            beatmap.getApproachRate(),
                            beatmap.getSlideMultiplier(),
                            beatmap.getSliderTickRate(),
                            beatmap.getStarRating(),
                            beatmapSetDto
                    );
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
        BeatmapSetDto beatmapSetDto = request.getBeatmapSetDto();

        if (beatmapSetDto == null) {
            return Result.failure(Error.badRequest("Beatmap set data is missing"));
        }

        try {
            beatmapSetRepository.insertBeatmapSet(
                    beatmapSetDto.getId(),
                    beatmapSetDto.getTitle(),
                    beatmapSetDto.getArtist(),
                    beatmapSetDto.getCreator(),
                    beatmapSetDto.getLength(),
                    beatmapSetDto.getBpm()
            );

            String message = "Beatmap set inserted successfully with ID: " + beatmapSetDto.getId();
            return Result.success(new InsertBeatmapSetResponse(true, message));

        } catch (RuntimeException e) {
            return Result.failure(Error.internal("Database error: " + e.getMessage()));
        }
    }

    public Result<InsertBeatmapSetResponse> insertBeatmap(InsertBeatmapRequest request) {
        try {
            BeatmapDto beatmapDto = request.getBeatmapDto();

            if (beatmapDto == null) {
                return Result.failure(Error.badRequest("Beatmap data is missing"));
            }

            BeatmapSet beatmapSet = beatmapSetRepository.getBeatmapSetById(beatmapDto.getBeatmapSetId());

            if (beatmapSet == null) {
                return Result.failure(Error.notFound("Beatmap set not found for ID: " + beatmapDto.getBeatmapSetId()));
            }

            beatmapRepository.insertBeatmap(
                    beatmapDto.getId(),
                    beatmapDto.getBeatmapSetId(),
                    beatmapDto.getVersion(),
                    beatmapDto.getHpDrainRate(),
                    beatmapDto.getCircleSize(),
                    beatmapDto.getOverallDifficulty(),
                    beatmapDto.getApproachRate(),
                    beatmapDto.getSlideMultiplier(),
                    beatmapDto.getSliderTickRate(),
                    beatmapDto.getStarRating()
            );

            String message = "Beatmap inserted successfully with ID: " + beatmapDto.getId();
            return Result.success(new InsertBeatmapSetResponse(true, message));

        } catch (RuntimeException e) {
            return Result.failure(Error.internal("Database error: " + e.getMessage()));
        }
    }
}
