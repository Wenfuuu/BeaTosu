package beat.osu.shared.dto.match.events;

import beat.osu.shared.dto.match.MatchDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class MatchStartedEvent implements Serializable {
    private int matchId;
    private MatchDto matchDto;
}
