package beat.osu.server.entities;

import beat.osu.shared.enums.match.MatchWinCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    private int id;
    private String name;
    private String password;
    private boolean inProgress;
    private boolean isChangingBeatmap;
    private int maxPlayerCount;
    private int beatmapId;
    private MatchWinCondition winCondition;
}
