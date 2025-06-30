package beat.osu.shared.dto.match.events;

import java.io.Serializable;

import lombok.Data;

@Data
public class PlayerKickedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private int matchId;
    private int kickedUserId;
    private long timestamp;

    public PlayerKickedEvent(int matchId, int kickedUserId) {
        this.matchId = matchId;
        this.kickedUserId = kickedUserId;
        this.timestamp = System.currentTimeMillis();
    }
}
