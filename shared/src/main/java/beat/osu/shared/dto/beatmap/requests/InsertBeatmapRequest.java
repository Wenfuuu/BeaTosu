package beat.osu.shared.dto.beatmap.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertBeatmapRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int beatmapSetId;
    private String version;
    private double hpDrainRate;
    private double circleSize;
    private double overallDifficulty;
    private double approachRate;
    private double sliderMultiplier;
    private double sliderTickRate;
    private double starRating;
}