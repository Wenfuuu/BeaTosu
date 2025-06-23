package beat.osu.client.events.game;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ReplayEvent {
    private long timeDelta;
    private double x;
    private double y;
    private int keyMask;
}
