package beat.osu.client.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScoreChangeData {
    private int score;
    private int scoreIncrease;
}
