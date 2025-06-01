package beat.osu.server.service;

import beat.osu.server.entities.Beatmap;
import beat.osu.server.entities.BeatmapSet;
import beat.osu.server.repositories.BeatmapRepository;
import beat.osu.server.repositories.BeatmapSetRepository;
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

    public GetAllBeatmapsResponse getAllBeatmaps() {
        ArrayList<Beatmap> beatmaps = beatmapRepository.getAllBeatmaps();
        ArrayList<BeatmapSet> beatmapSets = beatmapSetRepository.getAllBeatmapSets();

        Map<Integer, BeatmapSet> setMap = beatmapSets.stream()
                .collect(Collectors.toMap(BeatmapSet::getId, set -> set));

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
            }
        }

        return new GetAllBeatmapsResponse(beatmapDtos);
    }
}
