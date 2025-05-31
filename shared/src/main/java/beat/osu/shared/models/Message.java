package beat.osu.shared.models;

import beat.osu.shared.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private MessageType type;
    private String action;
    private Object payload;
    private Long timestamp;

    public Message(MessageType type, String action, Object payload, Long timestamp) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.action = action;
        this.payload = payload;
        this.timestamp = timestamp;
    }
}