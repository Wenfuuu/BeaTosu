package beat.osu.client.model;

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

    public int getDifficultyMultiplier(int objectCount, double drainTimeInSeconds) {
        double hitObjectDensity = objectCount / drainTimeInSeconds * 8.0;
        double clampedDensity = Math.max(0, Math.min(hitObjectDensity, 16));

        double rawDifficulty = hpDrainRate + circleSize + overallDifficulty + clampedDensity;
        double normalized = rawDifficulty / 38.0 * 5.0;

        return (int) Math.round(normalized);
    }
}
