package beat.osu.shared.dto.match.events;

import java.io.Serializable;

import lombok.Data;

@Data
public class HostChangedEvent implements Serializable {
    private int matchId;
    private int newHostUserId;
    private int previousHostUserId;
    private long timestamp;

    public HostChangedEvent(int matchId, int newHostUserId, int previousHostUserId) {
        this.matchId = matchId;
        this.newHostUserId = newHostUserId;
        this.previousHostUserId = previousHostUserId;
        this.timestamp = System.currentTimeMillis();
    }
}
