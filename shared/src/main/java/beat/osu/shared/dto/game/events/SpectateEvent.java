package beat.osu.shared.dto.game.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class SpectateEvent implements Serializable {
    private long currentTime;
    private double x;
    private double y;
    private int keyMask;
}
