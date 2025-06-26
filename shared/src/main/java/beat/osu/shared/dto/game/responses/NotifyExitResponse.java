package beat.osu.shared.dto.game.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class NotifyExitResponse implements Serializable {
    private String message;
}
