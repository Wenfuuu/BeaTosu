package beat.osu.client.interfaces;

import beat.osu.client.game.GameEvent;

public interface Subject {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers(GameEvent event);
}
