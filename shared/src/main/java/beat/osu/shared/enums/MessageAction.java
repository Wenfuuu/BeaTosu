package beat.osu.shared.enums;

import java.io.Serializable;

public enum MessageAction implements Serializable {
    // System actions
    DISCONNECT,
    GET_USER_COUNT,

    // Auth actions
    REGISTER,
    LOGIN,

    // Beatmap actions
    GET_ALL_BEATMAPS,
    INSERT_BEATMAP,
    INSERT_BEATMAP_SET,
}
