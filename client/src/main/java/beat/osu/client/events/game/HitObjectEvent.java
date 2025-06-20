package beat.osu.client.events.game;

import beat.osu.client.enums.HitResult;
import beat.osu.client.model.HitObject;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HitObjectEvent {
    private HitObject hitObject;
    private HitResult hitResult;
    private boolean perfectCombo;
    private boolean imperfectOrMissed;
}
