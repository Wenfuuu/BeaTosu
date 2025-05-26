package beat.osu.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BreakPoint {
    private int startTime;
    private int endTime;
}
