package beat.osu.shared.dto.chat.events;

import java.io.Serializable;

import beat.osu.shared.dto.user.UserDto;
import lombok.Data;

@Data
public class UserLeftChannelEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private int channelId;
    private int userId;
    private long timestamp;

    public UserLeftChannelEvent(int channelId, int userId) {
        this.channelId = channelId;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }
}
