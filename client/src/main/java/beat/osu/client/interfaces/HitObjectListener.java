package beat.osu.client.interfaces;

import beat.osu.client.enums.HitResult;
import beat.osu.client.model.HitObject;

public interface HitObjectListener {
    void onHit(HitObject hitObject, HitResult result);
    void onMiss(HitObject hitObject);
    void onAdditionalSpin(HitObject hitObject, int extraSpins);
}
