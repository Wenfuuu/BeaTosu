package beat.osu.shared.dto.game;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ReplayEvent implements Serializable {
    private long timeDelta;
    private double x;
    private double y;
    private int keyMask;
}
