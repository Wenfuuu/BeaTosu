package beat.osu.client.events.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InputOverlayData {
    private boolean key1Pressed;
    private boolean key2Pressed;
}
