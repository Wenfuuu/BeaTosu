package beat.osu.shared.dto.beatmap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeatmapSetDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String artist;
    private String creator;
    private String length;
    private int bpm;
}
