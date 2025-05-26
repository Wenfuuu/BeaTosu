package beat.osu.client.helper;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.Getter;

import java.io.File;

public class BgmManager {
    private static final String TEMP_DIR = "./src/main/resources/assets/temp/";

    @Getter
    private static MediaPlayer currentPlayer;

    public static void playGameBgm() {
        String bgmPath = TEMP_DIR + "audio.mp3";
        stopBgm();

        File audioFile = new File(bgmPath);
        if (!audioFile.exists()) {
            System.err.println("BGM file not found: " + audioFile.getAbsolutePath());
            return;
        }

        Media media = new Media(audioFile.toURI().toString());
        currentPlayer = new MediaPlayer(media);
//        currentPlayer.setOnEndOfMedia(() -> {
//            currentPlayer.seek(Duration.ZERO);
//            currentPlayer.play();
//        });
//        currentPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        currentPlayer.setAutoPlay(true);
        currentPlayer.setVolume(0.2);
    }

    public static void playBgm(String filePath) {
        stopBgm(); // Stop if already playing

        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            System.err.println("BGM file not found: " + audioFile.getAbsolutePath());
            return;
        }

        Media media = new Media(audioFile.toURI().toString());
        currentPlayer = new MediaPlayer(media);
        currentPlayer.setAutoPlay(true);
        currentPlayer.setVolume(0.2);
    }

    public static void pauseBgm() {
        if (currentPlayer != null) {
            currentPlayer.pause();
        }
    }

    public static void resumeBgm() {
        if (currentPlayer != null) {
            currentPlayer.play();
        }
    }

    public static void stopBgm() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer.dispose();
            currentPlayer = null;
        }
    }

    public static void setVolume(double volume) {
        if (currentPlayer != null) {
            currentPlayer.setVolume(volume);
        }
    }

    public static boolean isPlaying() {
        return currentPlayer != null && currentPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }
}
