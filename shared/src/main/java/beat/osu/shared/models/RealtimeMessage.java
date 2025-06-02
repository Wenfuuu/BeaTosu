package beat.osu.shared.models;

import beat.osu.shared.enums.RealtimeMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RealtimeMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private RealtimeMessageType type;
    private String fromClientId;
    private Object payload;
    private Long timestamp;
}