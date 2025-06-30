package beat.osu.shared.models;

import beat.osu.shared.enums.message.MessageAction;
import beat.osu.shared.enums.message.MessageType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
public class RequestMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String requestId;
    private MessageType type;
    private MessageAction action;
    private Object payload;
    private Long timestamp;

    public RequestMessage(MessageType type, MessageAction action, Object payload) {
        this.requestId = UUID.randomUUID().toString();
        this.type = type;
        this.action = action;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }
}