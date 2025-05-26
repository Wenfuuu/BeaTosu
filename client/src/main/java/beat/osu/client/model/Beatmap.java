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

    private double getDrainTimeInSeconds(ArrayList<HitObject> hitObjects, ArrayList<BreakPoint> breakPoints) {
        int totalBreakTime = 0;
        for (BreakPoint breakPoint : breakPoints) {
            totalBreakTime += breakPoint.getEndTime() - breakPoint.getStartTime();
        }

        int startTime = (int) hitObjects.get(0).getHitTime();
        int endTime = (int) hitObjects.get(hitObjects.size() - 1).getHitTime();
        int totalTime = endTime - startTime - totalBreakTime;

        return totalTime / 1000.0;
    }

    public int getDifficultyMultiplier(ArrayList<HitObject> hitObjects, ArrayList<BreakPoint> breakPoints) {
        double drainTimeInSeconds = getDrainTimeInSeconds(hitObjects, breakPoints);
        double hitObjectDensity = hitObjects.size() / drainTimeInSeconds * 8.0;
        double clampedDensity = Math.max(0, Math.min(hitObjectDensity, 16));

        double rawDifficulty = hpDrainRate + circleSize + overallDifficulty + clampedDensity;
        double normalized = rawDifficulty / 38.0 * 5.0;

        return (int) Math.round(normalized);
    }
}
