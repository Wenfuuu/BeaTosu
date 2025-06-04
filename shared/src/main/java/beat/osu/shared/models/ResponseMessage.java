package beat.osu.shared.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMessage implements Serializable {
    private String requestId;
    private Object payload;   // Result or Error
    private Long timestamp;
}