package beat.osu.client.helper;

import beat.osu.client.utils.OsuParser;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BgmManager {
    private static final String TEMP_DIR = "./src/main/resources/assets/temp/";
    private static String currentBgmHash = null;

    @Getter
    private static MediaPlayer currentPlayer;

    private static String computeFileHash(File file) {
        try (InputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void playPreviewBgm() {
        String bgmPath = TEMP_DIR + "audio.mp3";

        File audioFile = new File(bgmPath);
        if (!audioFile.exists()) {
            System.err.println("BGM file not found: " + audioFile.getAbsolutePath());
            return;
        }

        String newHash = computeFileHash(audioFile);
        System.out.println(newHash);
        if (newHash != null && newHash.equals(currentBgmHash)) {
            System.out.println("Same BGM content. Skipping playback.");
            return;
        }

        stopBgm();
        Media media = new Media(audioFile.toURI().toString());
        currentPlayer = new MediaPlayer(media);

        currentPlayer.setOnReady(() -> {
            Duration previewTime = new Duration(OsuParser.getPreviewTime());
            currentPlayer.seek(previewTime);

            // Loop from preview time
            currentPlayer.setOnEndOfMedia(() -> {
                currentPlayer.seek(previewTime);
                currentPlayer.play();
            });
        });

        currentPlayer.setAutoPlay(true);
        currentPlayer.setVolume(0.2);

        currentBgmHash = newHash;
    }

    public static void prepareGameBgm() {
        String bgmPath = TEMP_DIR + "audio.mp3";
        File audioFile = new File(bgmPath);

        if (!audioFile.exists()) {
            System.err.println("BGM file not found: " + audioFile.getAbsolutePath());
            return;
        }

        stopBgm();
        Media media = new Media(audioFile.toURI().toString());
        currentPlayer = new MediaPlayer(media);
        currentPlayer.setVolume(0.2);
        currentPlayer.setAutoPlay(false);

        currentPlayer.setOnError(() -> {
            System.err.println("MediaPlayer error: " + currentPlayer.getError());
        });
        media.setOnError(() -> {
            System.err.println("Media error: " + media.getError());
        });

        currentPlayer.setOnReady(() -> {
            System.out.println("BGM ready for playback");
        });
    }

    public static void playGameBgm() {
        if(currentPlayer != null) {
            System.out.println("Playing game BGM");
            currentPlayer.play();
        }
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
