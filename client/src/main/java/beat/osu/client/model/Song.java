package beat.osu.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Song {
    private String title;
    private String artist;
    private String audioPath;
}
