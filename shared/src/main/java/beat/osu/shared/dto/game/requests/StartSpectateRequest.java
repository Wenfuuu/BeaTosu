package beat.osu.shared.dto.game.requests;

import beat.osu.shared.dto.game.SpectateDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class StartSpectateRequest implements Serializable {
    private SpectateDto spectateDto;
}
