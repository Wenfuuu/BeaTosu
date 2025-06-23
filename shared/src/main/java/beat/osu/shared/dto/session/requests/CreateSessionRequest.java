package beat.osu.shared.dto.session.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class CreateSessionRequest implements Serializable {
    private int userId;
    private String key;
    private Object value;
}
