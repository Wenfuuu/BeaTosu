package beat.osu.client.controller;

import beat.osu.client.database.BeatmapRepository;
import beat.osu.client.model.Beatmap;

import java.util.ArrayList;

public class BeatmapController {
    private final BeatmapRepository beatmapRepository;

    public BeatmapController() {
        this.beatmapRepository = new BeatmapRepository();
    }

    public ArrayList<Beatmap> fetchBeatmaps() {
        return beatmapRepository.fetchBeatmaps();
    }

    public void insertBeatmapSet(
            int beatmapSetId,
            String title,
            String artist,
            String creator,
            String length,
            int bpm,
            String backgroundFile
    ) {
        beatmapRepository.insertBeatmapSet(beatmapSetId, title, artist, creator, length, bpm, backgroundFile);
    }

    public void insertBeatmap(
            int beatmapId,
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
        beatmapRepository.insertBeatmap(beatmapId, beatmapSetId, version, hpDrainRate, circleSize, overallDifficulty, approachRate, slideMultiplier, sliderTickRate, starRating);
    }
}
