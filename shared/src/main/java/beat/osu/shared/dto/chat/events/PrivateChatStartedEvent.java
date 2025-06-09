package beat.osu.shared.dto.chat.events;

import beat.osu.shared.dto.chat.PrivateChatDto;
import lombok.Data;

import java.io.Serializable;

@Data
public class PrivateChatStartedEvent implements Serializable {
    private PrivateChatDto privateChat;
    private int userId;
    private long timestamp;

    public PrivateChatStartedEvent(PrivateChatDto privateChat, int userId) {
        this.privateChat = privateChat;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }
}
