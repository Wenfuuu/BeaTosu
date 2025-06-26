package beat.osu.shared.dto.beatmap.responses;

import java.io.Serializable;

import beat.osu.shared.dto.beatmap.BeatmapDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetBeatmapByIdResponse implements Serializable {
    private BeatmapDto beatmap;
}
