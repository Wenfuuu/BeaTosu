package beat.osu.shared.enums;

import java.io.Serializable;

public enum RealtimeMessageType implements Serializable {
    ADD_CONNECTED_USER,
    REMOVE_CONNECTED_USER,

    CHAT_MESSAGE,
    SYSTEM_NOTIFICATION,
}