package beat.osu.shared.models;

import beat.osu.shared.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
    private MessageType type;
    private String action;
    private Object payload;
    private String sessionId;
    private Long timestamp;
}