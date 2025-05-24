package beat.osu.beatosu.interfaces;

import beat.osu.beatosu.game.GameEvent;

public interface Observer {
    void update(GameEvent event);
}
