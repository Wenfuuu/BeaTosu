package beat.osu.shared.dto.match.events;

import java.io.Serializable;

import beat.osu.shared.dto.beatmap.BeatmapDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchBeatmapUpdatedEvent implements Serializable {
    private int matchId;
    private BeatmapDto newBeatmapDto;
    private long timestamp;

    public MatchBeatmapUpdatedEvent(int matchId, BeatmapDto newBeatmapDto) {
        this.matchId = matchId;
        this.newBeatmapDto = newBeatmapDto;
        this.timestamp = System.currentTimeMillis();
    }
}
