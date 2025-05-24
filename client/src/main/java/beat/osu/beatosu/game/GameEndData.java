package beat.osu.beatosu.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameEndData {
    private int finalScore;
    private int totalHits;
    private int totalMisses;
    private double accuracy;
}
