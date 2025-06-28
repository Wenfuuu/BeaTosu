package beat.osu.shared.dto.match.requests;

import beat.osu.shared.dto.match.events.PlayerFinishedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class PlayerFinishedEventRequest implements Serializable {
    private PlayerFinishedEvent playerFinishedEvent;
}
