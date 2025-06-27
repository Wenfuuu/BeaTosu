package beat.osu.shared.dto.match.events;

import java.io.Serializable;

import lombok.Data;

@Data
public class MatchEndedEvent implements Serializable {
    private int matchId;
    private long timestamp;

    public MatchEndedEvent(int matchId) {
        this.matchId = matchId;
        this.timestamp = System.currentTimeMillis();
    }
}
