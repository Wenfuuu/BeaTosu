package beat.osu.server.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeatmapSet {
    private int id;
    private String title;
    private String artist;
    private String creator;
    private String length;
    private int bpm;
}
