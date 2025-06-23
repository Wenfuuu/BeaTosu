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

    MATCH_CREATED,
    USER_JOINED_MATCH,
    USER_LEFT_MATCH,
    PLAYER_KICKED_FROM_MATCH,

    SYSTEM_NOTIFICATION,

    SPECTATE_EVENT,
}