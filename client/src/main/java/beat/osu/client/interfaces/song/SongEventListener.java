package beat.osu.client.interfaces.song;

import beat.osu.client.events.song.SongChangeEvent;

public interface SongEventListener {
    void update(SongChangeEvent event);
}
