package beat.osu.beatosu.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Beatmap {
    private int beatmapId;
    private int beatmapSetId; //foreign key
    private String version;
    private double hpDrainRate;
    private double circleSize;
    private double overallDifficulty;
    private double approachRate;
    private double slideMultiplier;
    private double sliderTickRate;
    private double starRating;

    private BeatmapSet beatmapSet;
}
