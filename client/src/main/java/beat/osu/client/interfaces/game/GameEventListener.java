package beat.osu.client.interfaces.game;

import beat.osu.client.events.game.GameEvent;

public interface GameEventListener {
    void update(GameEvent event);
}
