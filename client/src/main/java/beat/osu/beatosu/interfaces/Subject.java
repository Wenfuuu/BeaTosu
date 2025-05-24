package beat.osu.beatosu.interfaces;

import beat.osu.beatosu.game.GameEvent;

public interface Subject {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers(GameEvent event);
}
