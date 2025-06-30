package beat.osu.shared.dto.game.requests;

import beat.osu.shared.dto.game.events.SpectateEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class SendSpectateEventRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private SpectateEvent spectateEvent;
}
