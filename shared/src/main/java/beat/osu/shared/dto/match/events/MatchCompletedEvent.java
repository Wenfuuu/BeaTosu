package beat.osu.shared.dto.match.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchCompletedEvent {
    private String message;
}
