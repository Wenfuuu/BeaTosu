package beat.osu.shared.dto.game.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopSpectateResponse implements Serializable {
    private String message;
}
