package beat.osu.shared.enums.message;

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
    GET_BEATMAP_BY_ID,
    INSERT_BEATMAP,
    INSERT_BEATMAP_SET,

    // Score actions
    GET_ALL_SCORES,
    INSERT_SCORE,

    // Channel actions
    GET_ALL_CHANNELS,
    GET_JOINED_CHANNELS,
    JOIN_CHANNEL,
    LEAVE_CHANNEL,
    SEND_CHANNEL_MESSAGE,

    // Private chat actions
    SEND_PRIVATE_CHAT_MESSAGE,

    // Match actions
    GET_ALL_MATCHES,
    CREATE_MATCH,
    JOIN_MATCH,
    LEAVE_MATCH,
    KICK_PLAYER,
    TRANSFER_HOST,
    CHANGE_MATCH_SLOT,
    UPDATE_MATCH_PASSWORD,
    UPDATE_MATCH_NAME,
    UPDATE_MATCH_WIN_CONDITION,
    START_MATCH,
    SEND_MATCH_SCORE_EVENT,

    // Session actions
    CREATE_SESSION_DATA,
    REMOVE_SESSION_DATA,
    GET_SESSION_DATA,

    // Spectate actions
    START_SPECTATE,
    SEND_SPECTATE_EVENT,
    STOP_SPECTATE,
    CHANGE_SPECTATE_STATUS,
    PLAYER_EXIT_GAME,
}
