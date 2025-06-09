package beat.osu.shared.dto.chat.events;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLeftPrivateChatEvent implements Serializable {
    private int privateChatId;
    private int userId;
    private long timestamp;

    public UserLeftPrivateChatEvent(int privateChatId, int userId) {
        this.privateChatId = privateChatId;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }
}
