package beat.osu.client.events.song;

import beat.osu.client.model.Song;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SongChangeEvent {
    private Song song;
}
