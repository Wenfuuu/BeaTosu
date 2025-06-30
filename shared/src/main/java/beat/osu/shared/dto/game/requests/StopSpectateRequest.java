package beat.osu.shared.dto.game.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class StopSpectateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
}
