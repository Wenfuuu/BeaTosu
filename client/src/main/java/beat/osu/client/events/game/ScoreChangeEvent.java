package beat.osu.client.events.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScoreChangeEvent {
    private int score;
    private int scoreIncrease;
}
