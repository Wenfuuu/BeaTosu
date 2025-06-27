package beat.osu.shared.dto.match.events;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotChangedEvent implements Serializable {
    private int matchId;
    private int userId;
    private int oldSlotIndex;
    private int newSlotIndex;
    private long timestamp;

    public SlotChangedEvent(int matchId, int userId, int oldSlotIndex, int newSlotIndex) {
        this.matchId = matchId;
        this.userId = userId;
        this.oldSlotIndex = oldSlotIndex;
        this.newSlotIndex = newSlotIndex;
        this.timestamp = System.currentTimeMillis();
    }
}
