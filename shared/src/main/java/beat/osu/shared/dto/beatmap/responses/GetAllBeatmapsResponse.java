package beat.osu.shared.dto.beatmap.responses;

import beat.osu.shared.dto.beatmap.BeatmapDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllBeatmapsResponse implements Serializable {
    private ArrayList<BeatmapDto> beatmaps;
}