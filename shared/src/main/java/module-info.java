module beat.osu.shared {
    requires static lombok;
    exports beat.osu.shared.common;
    exports beat.osu.shared.enums;
    exports beat.osu.shared.models;
    exports beat.osu.shared.dto.auth.requests;
    exports beat.osu.shared.dto.auth.responses;
    exports beat.osu.shared.dto.auth;
}