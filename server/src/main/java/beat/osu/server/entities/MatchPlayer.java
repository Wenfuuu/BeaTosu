package beat.osu.server.entities;

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
    private String role;        // "host", "player"
    private String status;      // "no_map", "ready", "not_ready", "playing"
    private int slotIndex;      // 0-15
}
