package beat.osu.client.helper;

import beat.osu.client.events.song.SongChangeEvent;
import beat.osu.client.interfaces.song.SongEventListener;
import beat.osu.client.interfaces.song.SongEventPublisher;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.Song;
import beat.osu.client.utils.OsuParser;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class BgmManager {
    private static String currentBgmHash = null;
    private static String defaultBgmHash;

    @Getter
    private static MediaPlayer currentPlayer;

    private static final ArrayList<SongEventListener> listeners = new ArrayList<>();

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

    public static boolean isSameBgm(String newHash) {
        if (currentBgmHash == null) {
            currentBgmHash = newHash;
            return false;
        }
        return currentBgmHash.equals(newHash);
    }

    private static void loopFromPreviewTime() {
        if (currentPlayer != null) {
            Duration previewTime = new Duration(OsuParser.getPreviewTime());
            currentPlayer.setOnEndOfMedia(() -> {
                currentPlayer.seek(previewTime);
                currentPlayer.play();
            });
        }
    }

    public static void playPreviewBgm(boolean fromAnotherPage) {
        Beatmap beatmap = OsuParser.getCurrentBeatmap();
        File tempDir = ResourceManager.getTempDirectory();
        File beatmapDir = new File(tempDir, String.valueOf(beatmap.getBeatmapSetId()));
        File audioFile = new File(beatmapDir, "audio.mp3");

        if (!audioFile.exists()) {
            System.err.println("BGM file not found: " + audioFile.getAbsolutePath());
            return;
        }

        String newHash = computeFileHash(audioFile);
        if(!fromAnotherPage) {
            if (isSameBgm(newHash)) {
                System.out.println("Same BGM content. Skipping playback.");
                return;
            }
            stopBgm();
        }else {
            if(newHash != null && currentBgmHash.equals(defaultBgmHash)) stopBgm();
            if (isSameBgm(newHash) && currentPlayer != null) {
                System.out.println("From another page, Same BGM content. Resuming BGM.");
                currentPlayer.play();
                loopFromPreviewTime();
                return;
            }
        }
        currentBgmHash = newHash;

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
    }

    public static void prepareGameBgm() {
        Beatmap beatmap = OsuParser.getCurrentBeatmap();
        File tempDir = ResourceManager.getTempDirectory();
        File beatmapDir = new File(tempDir, String.valueOf(beatmap.getBeatmapSetId()));
        File audioFile = new File(beatmapDir, "audio.mp3");

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

    public static void playBgm(File bgmFile) {
        stopBgm();
        defaultBgmHash = computeFileHash(bgmFile);
        currentBgmHash = defaultBgmHash;

        try {
            Media media = new Media(bgmFile.toURI().toString());
            currentPlayer = new MediaPlayer(media);
            currentPlayer.setAutoPlay(true);
            currentPlayer.setVolume(0.2);
        } catch (Exception e) {
            System.err.println("Failed to load BGM: " + bgmFile.getPath());
            e.printStackTrace();
        }
    }

    public static void playSong(Song song) {
        File songFile = new File(song.getAudioPath());
        playBgm(songFile);

        notifyListeners(new SongChangeEvent(song));
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

    public static void addListener(SongEventListener songEventListener) {
        listeners.add(songEventListener);
    }

    public static void removeListener(SongEventListener songEventListener) {
        listeners.remove(songEventListener);
    }

    private static void notifyListeners(SongChangeEvent event) {
        for (SongEventListener listener : listeners) {
            listener.update(event);
        }
    }
}
