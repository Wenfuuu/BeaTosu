package beat.osu.client.model;

import beat.osu.client.utils.OsuParser;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;

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

    double getDrainTimeInSeconds(ArrayList<BreakPoint> breakPoints) {
        int startTime = breakPoints.get(0).getStartTime();
        int endTime = breakPoints.get(breakPoints.size() - 1).getEndTime();

        return (endTime - startTime) / 1000.0;
    }

    public int getDifficultyMultiplier(int objectCount, ArrayList<BreakPoint> breakPoints) {
        double drainTimeInSeconds = getDrainTimeInSeconds(breakPoints);
        double hitObjectDensity = objectCount / drainTimeInSeconds * 8.0;
        double clampedDensity = Math.max(0, Math.min(hitObjectDensity, 16));

        double rawDifficulty = hpDrainRate + circleSize + overallDifficulty + clampedDensity;
        double normalized = rawDifficulty / 38.0 * 5.0;

        return (int) Math.round(normalized);
    }
}
