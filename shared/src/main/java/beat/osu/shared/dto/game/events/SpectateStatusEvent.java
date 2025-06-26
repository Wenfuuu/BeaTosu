package beat.osu.shared.dto.game.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class SpectateStatusEvent implements Serializable {
    private boolean isPaused;
}
