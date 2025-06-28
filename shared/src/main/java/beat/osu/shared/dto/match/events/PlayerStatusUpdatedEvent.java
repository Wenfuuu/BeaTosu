package beat.osu.shared.dto.match.events;

import java.io.Serializable;

import beat.osu.shared.enums.match.PlayerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatusUpdatedEvent implements Serializable {
    private int matchId;
    private int userId;
    private PlayerStatus newStatus;
}
