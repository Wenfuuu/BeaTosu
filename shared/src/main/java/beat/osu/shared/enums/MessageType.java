package beat.osu.shared.enums;

import java.io.Serializable;

public enum MessageType implements Serializable {
    SYSTEM,
    USER,
    AUTH,
    BEATMAP,
    CHANNEL,
    PRIVATE_CHAT,
}