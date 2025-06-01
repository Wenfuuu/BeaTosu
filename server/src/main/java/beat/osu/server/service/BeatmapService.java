package beat.osu.server.service;

import beat.osu.server.entities.Beatmap;
import beat.osu.server.entities.BeatmapSet;
import beat.osu.server.repositories.BeatmapRepository;
import beat.osu.server.repositories.BeatmapSetRepository;
import beat.osu.shared.common.Error;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.BeatmapDto;
import beat.osu.shared.dto.beatmap.BeatmapSetDto;
import beat.osu.shared.dto.beatmap.responses.GetAllBeatmapsResponse;
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
}
