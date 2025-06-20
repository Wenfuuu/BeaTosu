package beat.osu.client.events.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComboChangeEvent {
    private int combo;
    private boolean comboBreak;
}
