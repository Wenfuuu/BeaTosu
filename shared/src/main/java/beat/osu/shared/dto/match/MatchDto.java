package beat.osu.shared.dto.match;

import java.io.Serializable;
import java.util.List;

import beat.osu.shared.enums.match.MatchWinCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchDto implements Serializable {
    private int id;
    private String name;
    private String password;
    private boolean inProgress;
    private int maxPlayerCount;

    private int beatmapId;
    private String beatmapName;

    private int lowestRank;
    private int highestRank;

    private MatchWinCondition winCondition;

    private List<MatchPlayerDto> players;
}
