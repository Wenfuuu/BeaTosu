package beat.osu.client.game;

import beat.osu.client.enums.GameEventType;
import lombok.Getter;

@Getter
public class GameEvent {
    private final GameEventType type;
    private final Object data;
    private final long timestamp;

    public GameEvent(GameEventType type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // Type-safe getters for common data types
    @SuppressWarnings("unchecked")
    public <T> T getData(Class<T> type) {
        if (type.isInstance(data)) {
            return (T) data;
        }
        return null;
    }
}
