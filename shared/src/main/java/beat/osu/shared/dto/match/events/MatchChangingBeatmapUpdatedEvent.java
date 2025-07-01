package beat.osu.shared.dto.match.events;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchChangingBeatmapUpdatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private int matchId;
    private boolean isChangingBeatmap;
    private long timestamp;

    public MatchChangingBeatmapUpdatedEvent(int matchId, boolean isChangingBeatmap) {
        this.matchId = matchId;
        this.isChangingBeatmap = isChangingBeatmap;
        this.timestamp = System.currentTimeMillis();
    }
}
