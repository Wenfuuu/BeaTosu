package beat.osu.shared.dto.match.events;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchPasswordUpdatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private int matchId;
    private String newPassword;
    private long timestamp;

    public MatchPasswordUpdatedEvent(int matchId, String newPassword) {
        this.matchId = matchId;
        this.newPassword = newPassword;
        this.timestamp = System.currentTimeMillis();
    }
}
