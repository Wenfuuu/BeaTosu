package beat.osu.shared.enums;

import java.io.Serializable;

public enum RealtimeMessageType implements Serializable {
    USER_CONNECTED,
    USER_DISCONNECTED,

    USER_JOINED_CHANNEL,
    USER_LEFT_CHANNEL,
    CHANNEL_MESSAGE,

    SYSTEM_NOTIFICATION,
}