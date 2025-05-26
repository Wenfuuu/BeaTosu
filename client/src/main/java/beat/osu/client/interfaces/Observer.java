package beat.osu.client.interfaces;

import beat.osu.client.game.GameEvent;

public interface Observer {
    void update(GameEvent event);
}
