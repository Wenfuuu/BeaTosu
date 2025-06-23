module beat.osu.shared {
    requires static lombok;

    exports beat.osu.shared.common;
    exports beat.osu.shared.enums;
    exports beat.osu.shared.models;

    exports beat.osu.shared.dto.auth.requests;
    exports beat.osu.shared.dto.auth.responses;

    exports beat.osu.shared.dto.beatmap;
    exports beat.osu.shared.dto.beatmap.requests;
    exports beat.osu.shared.dto.beatmap.responses;

    exports beat.osu.shared.dto.chat;
    exports beat.osu.shared.dto.chat.requests;
    exports beat.osu.shared.dto.chat.responses;
    exports beat.osu.shared.dto.chat.events;

    exports beat.osu.shared.dto.match;
    exports beat.osu.shared.dto.match.requests;
    exports beat.osu.shared.dto.match.responses;
    exports beat.osu.shared.dto.match.events;

    exports beat.osu.shared.dto.system.responses;

    exports beat.osu.shared.dto.user;
    exports beat.osu.shared.dto.user.events;

    exports beat.osu.shared.dto.score;
    exports beat.osu.shared.dto.score.requests;
    exports beat.osu.shared.dto.score.responses;
    exports beat.osu.shared.dto.game;
    exports beat.osu.shared.dto.game.events;
    exports beat.osu.shared.dto.game.requests;
    exports beat.osu.shared.dto.game.responses;
    exports beat.osu.shared.dto.session.requests;
    exports beat.osu.shared.dto.session.responses;
}