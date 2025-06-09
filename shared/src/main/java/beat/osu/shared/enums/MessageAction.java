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

    // Channel actions
    GET_ALL_CHANNELS,
    GET_JOINED_CHANNELS,
    JOIN_CHANNEL,
    LEAVE_CHANNEL,
    SEND_CHANNEL_MESSAGE,

    // Private chat actions
    GET_PRIVATE_CHATS,
    START_PRIVATE_CHAT,
    LEAVE_PRIVATE_CHAT,
    SEND_PRIVATE_CHAT_MESSAGE,
}
