package beat.osu.shared.enums;

import java.io.Serializable;

public enum MessageAction implements Serializable {
    // System actions
    DISCONNECT,
    GET_CONNECTED_USERS,

    // Auth actions
    REGISTER,
    LOGIN,

    // Beatmap actions
    GET_ALL_BEATMAPS,
    INSERT_BEATMAP,
    INSERT_BEATMAP_SET,

    // Message actions
    GET_ALL_CHANNELS,
    JOIN_CHANNEL,
    LEAVE_CHANNEL,
    SEND_CHANNEL_MESSAGE,
}
