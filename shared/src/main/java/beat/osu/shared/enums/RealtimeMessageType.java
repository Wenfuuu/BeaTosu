package beat.osu.shared.enums;

import java.io.Serializable;

public enum RealtimeMessageType implements Serializable {
    USER_CONNECTED,
    USER_DISCONNECTED,

    USER_JOINED_CHANNEL,
    USER_LEFT_CHANNEL,
    CHANNEL_MESSAGE,

    PRIVATE_CHAT_STARTED,
    LEFT_PRIVATE_CHAT,
    PRIVATE_CHAT_MESSAGE,

    SYSTEM_NOTIFICATION,
}