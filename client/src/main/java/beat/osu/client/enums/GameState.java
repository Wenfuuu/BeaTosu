package beat.osu.client.enums;

public enum GameState {
    NOT_STARTED,
    PLAYING,
    PAUSED,
    COMPLETED,   // Game has ended successfully
    FAILED,      // Game has ended due to failure (e.g., health dropped to zero)
    BREAK_PERIOD // Break period during gameplay
}
