package beat.osu.client.events.game;

import beat.osu.client.model.HitObject;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdditionalSpinEvent {
    private HitObject hitObject;
    private int additionalSpin;
}
