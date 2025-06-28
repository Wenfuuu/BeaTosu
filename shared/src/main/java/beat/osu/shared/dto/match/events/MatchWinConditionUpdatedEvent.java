package beat.osu.shared.dto.match.events;

import java.io.Serializable;

import beat.osu.shared.enums.match.MatchWinCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchWinConditionUpdatedEvent implements Serializable {
    private int matchId;
    private MatchWinCondition newWinCondition;
    private long timestamp;

    public MatchWinConditionUpdatedEvent(int matchId, MatchWinCondition newWinCondition) {
        this.matchId = matchId;
        this.newWinCondition = newWinCondition;
        this.timestamp = System.currentTimeMillis();
    }
}
