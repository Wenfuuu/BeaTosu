package beat.osu.shared.dto.match.events;

import beat.osu.shared.dto.match.MatchPlayerDto;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserJoinedMatchEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private int matchId;
    private MatchPlayerDto matchPlayer;
    private long timestamp;

    public UserJoinedMatchEvent(int matchId, MatchPlayerDto matchPlayer) {
        this.matchId = matchId;
        this.matchPlayer = matchPlayer;
        this.timestamp = System.currentTimeMillis();
    }
}