package beat.osu.beatosu.enums;

public enum GameState {
    NOT_STARTED,
    PLAYING,
    PAUSED,
    COMPLETED,   // Game has ended successfully
    FAILED,      // Game has ended due to failure (e.g., health dropped to zero)
    EXITED       // Game was exited before completion
}
