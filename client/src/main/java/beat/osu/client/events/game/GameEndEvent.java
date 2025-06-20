package beat.osu.client.events.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameEndEvent {
    private int score;
    private int perfectHits;
    private int gekiHits;
    private int greatHits;
    private int katuHits;
    private int goodHits;
    private int misses;
    private int highestCombo;
    private double accuracy;
    private String grade;
}
