package beat.osu.server.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateChat {
    private int id;
    private int userIdA;
    private int userIdB;

    public boolean isParticipant(int userId) {
        return userId == userIdA || userId == userIdB;
    }

    public int getOtherUserId(int userId) {
        if (userId == userIdA) return userIdB;
        if (userId == userIdB) return userIdA;
        throw new IllegalArgumentException("User " + userId + " is not a participant in this chat");
    }
}
