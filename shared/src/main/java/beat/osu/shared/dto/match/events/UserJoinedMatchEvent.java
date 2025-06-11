package beat.osu.shared.dto.match.events;

import beat.osu.shared.dto.match.MatchDto;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserJoinedMatchEvent implements Serializable {
    private MatchDto match;
    private int userId;
    private long timestamp;

    public UserJoinedMatchEvent(MatchDto match, int userId) {
        this.match = match;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }
}