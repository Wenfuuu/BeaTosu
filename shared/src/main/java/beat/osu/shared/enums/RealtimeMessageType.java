package beat.osu.shared.enums;

import java.io.Serializable;

public enum RealtimeMessageType implements Serializable {
    USER_JOINED,
    USER_LEFT,
    USER_COUNT_UPDATE,

    CHAT_MESSAGE,
    SYSTEM_NOTIFICATION,
}