package beat.osu.shared.dto.match.events;

import java.io.Serializable;

import lombok.Data;

@Data
public class MatchEndedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private int matchId;
    private long timestamp;

    public MatchEndedEvent(int matchId) {
        this.matchId = matchId;
        this.timestamp = System.currentTimeMillis();
    }
}
