package beat.osu.beatosu.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BeatmapSet {
    private int beatmapSetId;
    private String title;
    private String artist;
    private String creator;
    private String length;
    private int bpm;
    private String backgroundFile;
}
