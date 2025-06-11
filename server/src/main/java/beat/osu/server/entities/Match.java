package beat.osu.server.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    private int id;
    private String name;
    private String password;
    private String status;
    private int maxPlayerCount;
    private int beatmapId;

    private List<MatchPlayer> players;
}
