package beat.osu.client.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReplayEventData {
    private long timeDelta;
    private double x;
    private double y;
    private int keyMask;
}
