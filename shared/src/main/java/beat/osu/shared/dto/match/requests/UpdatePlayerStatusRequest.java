package beat.osu.shared.dto.match.requests;

import java.io.Serializable;

import beat.osu.shared.enums.match.PlayerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlayerStatusRequest implements Serializable {
    private int matchId;
    private PlayerStatus newStatus;
}
