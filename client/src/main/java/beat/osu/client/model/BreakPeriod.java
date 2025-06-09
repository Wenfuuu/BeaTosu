package beat.osu.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BreakPeriod {
    private int startTime;
    private int endTime;
}
