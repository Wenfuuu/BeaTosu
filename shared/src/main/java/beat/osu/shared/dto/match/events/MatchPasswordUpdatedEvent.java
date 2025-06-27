package beat.osu.shared.dto.match.events;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchPasswordUpdatedEvent implements Serializable {
    private int matchId;
    private long timestamp;

    public MatchPasswordUpdatedEvent(int matchId) {
        this.matchId = matchId;
        this.timestamp = System.currentTimeMillis();
    }
}
