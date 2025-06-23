package beat.osu.shared.dto.game.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateSessionRequest {
    private String key;
    private Object value;
}
