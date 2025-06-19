package beat.osu.client.interfaces.game;

import beat.osu.client.events.game.GameEvent;

public interface GameEventPublisher {
    void addListener(GameEventListener gameEventListener);
    void removeListener(GameEventListener gameEventListener);
    void notifyListeners(GameEvent event);
}
