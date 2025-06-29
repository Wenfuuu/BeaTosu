package beat.osu.shared.dto.match.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class MatchCompletedEvent implements Serializable {
    private int matchId;
}
