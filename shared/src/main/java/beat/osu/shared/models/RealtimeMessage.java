package beat.osu.shared.models;

import java.io.Serializable;

import beat.osu.shared.enums.message.RealtimeMessageType;
import lombok.Data;

@Data
public class RealtimeMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private RealtimeMessageType type;
    private String fromClientId;
    private Object payload;
    private Long timestamp;

    public RealtimeMessage(RealtimeMessageType type, String fromClientId, Object payload) {
        this.type = type;
        this.fromClientId = fromClientId;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }
}