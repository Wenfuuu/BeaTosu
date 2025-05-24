package beat.osu.beatosu.game;

import beat.osu.beatosu.enums.HitResult;
import beat.osu.beatosu.model.HitObject;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HitObjectEventData {
    private HitObject hitObject;
    private long timingError;
    private HitResult hitResult;
}
