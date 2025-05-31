package beat.osu.shared.models;

import beat.osu.shared.enums.MessageAction;
import beat.osu.shared.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private MessageAction action;
    private Object payload;
    private Long timestamp;
}