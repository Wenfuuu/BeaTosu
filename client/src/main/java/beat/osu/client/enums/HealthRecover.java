package beat.osu.client.enums;

import lombok.Getter;

@Getter
public enum HealthRecover {
    PERFECT(10.0),
    GREAT(6.0),
    GOOD(2.0),
    SLIDER_TICK(2.0),
    SPIN(1.0),
    COMPLETE_SPIN(15.0),
    GEKI(15.0),
    PERFECT_KATU(12.5),
    GREAT_KATU(7.5);

    private final double hpRecover;

    HealthRecover(double hpRecover) { this.hpRecover = hpRecover; }
}
