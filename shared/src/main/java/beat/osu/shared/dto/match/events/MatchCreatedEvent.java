package beat.osu.shared.dto.match.events;

import beat.osu.shared.dto.match.MatchDto;
import lombok.Data;

import java.io.Serializable;

@Data
public class MatchCreatedEvent implements Serializable {
    private MatchDto match;
    private long timestamp;

    public MatchCreatedEvent(MatchDto match) {
        this.match = match;
        this.timestamp = System.currentTimeMillis();
    }
}
