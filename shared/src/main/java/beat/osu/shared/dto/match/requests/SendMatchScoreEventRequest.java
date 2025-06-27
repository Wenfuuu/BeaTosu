package beat.osu.shared.dto.match.requests;

import beat.osu.shared.dto.match.events.MatchScoreEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class SendMatchScoreEventRequest implements Serializable {
    private MatchScoreEvent matchScoreEvent;
}
