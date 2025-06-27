package beat.osu.shared.dto.match.events;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchNameUpdatedEvent implements Serializable {
    private int matchId;
    private String newName;
    private long timestamp;

    public MatchNameUpdatedEvent(int matchId, String newName) {
        this.matchId = matchId;
        this.newName = newName;
        this.timestamp = System.currentTimeMillis();
    }
}
