package beat.osu.shared.dto.beatmap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeatmapDto implements Serializable {
    private int id;
    private int beatmapSetId;
    private String version;
    private double hpDrainRate;
    private double circleSize;
    private double overallDifficulty;
    private double approachRate;
    private double slideMultiplier;
    private double sliderTickRate;
    private double starRating;

    private BeatmapSetDto beatmapSetDto;
}
