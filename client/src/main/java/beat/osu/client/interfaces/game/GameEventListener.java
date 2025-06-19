package beat.osu.client.interfaces.game;

import beat.osu.client.game.GameEvent;

public interface GameEventListener {
    void update(GameEvent event);
}
