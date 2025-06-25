package beat.osu.server.entities;

import beat.osu.shared.enums.match.PlayerRole;
import beat.osu.shared.enums.match.PlayerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchPlayer {
    private int id;
    private int matchId;
    private int userId;
    private PlayerRole role;          // "host", "player"
    private PlayerStatus status;      // "no_map", "ready", "not_ready", "playing"
    private int slotIndex;            // 0-15
}
