package beat.osu.shared.dto.match.events;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLeftMatchEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private int matchId;
    private int userId;
    private long timestamp;

    public UserLeftMatchEvent(int matchId, int userId) {
        this.matchId = matchId;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }
}
