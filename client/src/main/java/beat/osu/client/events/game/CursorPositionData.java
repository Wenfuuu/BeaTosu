package beat.osu.client.events.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CursorPositionData {
    private double x;
    private double y;
}
