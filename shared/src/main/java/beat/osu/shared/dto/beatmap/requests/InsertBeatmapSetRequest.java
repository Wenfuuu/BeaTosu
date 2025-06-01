package beat.osu.shared.dto.beatmap.requests;

import beat.osu.shared.dto.beatmap.BeatmapSetDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertBeatmapSetRequest implements Serializable {
    private BeatmapSetDto beatmapSetDto;
}