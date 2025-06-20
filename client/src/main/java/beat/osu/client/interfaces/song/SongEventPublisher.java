package beat.osu.client.interfaces.song;

import beat.osu.client.events.song.SongChangeEvent;

public interface SongEventPublisher {
    void addListener(SongEventListener songEventListener);
    void removeListener(SongEventListener songEventListener);
    void notifyListeners(SongChangeEvent event);
}