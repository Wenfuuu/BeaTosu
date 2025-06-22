package beat.osu.client.events.game;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GameEndEvent {
    private int score;
    private int highestCombo;
    private double accuracy;
    private int perfectHits;
    private int gekiHits;
    private int greatHits;
    private int katuHits;
    private int goodHits;
    private int misses;
    private String grade;
    private LocalDateTime date;
}
