package beat.osu.shared.dto.game.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class NotifyExitRequest implements Serializable {
    private String message;
}
