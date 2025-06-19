package beat.osu.client.events.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComboChangeData {
    private int combo;
    private boolean comboBreak;
}
