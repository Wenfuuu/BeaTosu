package beat.osu.server.entities;

import java.util.Objects;

import beat.osu.shared.enums.match.PlayerRole;
import beat.osu.shared.enums.match.PlayerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchPlayer {
    private int id;
    private int matchId;
    private int userId;
    private PlayerRole role;          // "host", "player"
    private PlayerStatus status;      // "no_map", "ready", "not_ready", "playing"
    private int slotIndex;            // 0-15

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchPlayer that = (MatchPlayer) o;
        return id == that.id && matchId == that.matchId && userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, matchId, userId);
    }

    @Override
    public String toString() {
        return userId + "(" + role + ")";
    }
}
