package beat.osu.shared.models;

import beat.osu.shared.enums.RealtimeMessageType;
import lombok.Data;

import java.io.Serializable;

@Data
public class RealtimeMessage implements Serializable {
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