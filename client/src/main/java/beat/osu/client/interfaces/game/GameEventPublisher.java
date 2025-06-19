package beat.osu.client.interfaces.game;

import beat.osu.client.events.game.GameEvent;

public interface GameEventPublisher {
    void addObserver(GameEventListener gameEventListener);
    void removeObserver(GameEventListener gameEventListener);
    void notifyObservers(GameEvent event);
}
