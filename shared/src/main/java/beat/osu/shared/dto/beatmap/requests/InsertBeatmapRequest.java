package beat.osu.shared.dto.beatmap.requests;

import beat.osu.shared.dto.beatmap.BeatmapDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertBeatmapRequest implements Serializable {
    private BeatmapDto beatmapDto;
}