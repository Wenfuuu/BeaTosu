package beat.osu.shared.dto.game.requests;

import beat.osu.shared.dto.game.events.SpectateStatusEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class NotifySpectateStatusRequest implements Serializable {
    private SpectateStatusEvent spectateStatusEvent;
}
