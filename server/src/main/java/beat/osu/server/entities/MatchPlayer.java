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
    private String role;
}
