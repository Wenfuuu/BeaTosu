package beat.osu.client.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BeatmapSet;
import beat.osu.client.model.Song;
import beat.osu.client.utils.OsuParser;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.Getter;

@Getter
public class BgmManager {
    private static volatile BgmManager instance;

    private String currentBgmHash = null;
    private String defaultBgmHash;
    private MediaPlayer currentPlayer;

    private BgmManager() {

    }

    public static BgmManager getInstance() {
        if (instance == null) {
            synchronized (BgmManager.class) {
                if (instance == null) {
                    instance = new BgmManager();
                }
            }
        }
        return instance;
    }

    private String computeFileHash(File file) {
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

    public boolean isSameBgm(String newHash) {
        if (currentBgmHash == null) {
            currentBgmHash = newHash;
            return false;
        }
        return currentBgmHash.equals(newHash);
    }

    private void loopFromPreviewTime() {
        if (currentPlayer != null) {
            Duration previewTime = new Duration(OsuParser.getPreviewTime());
            currentPlayer.setOnEndOfMedia(() -> {
                currentPlayer.seek(previewTime);
                currentPlayer.play();
            });
        }
    }

    public void playPreviewBgm(boolean fromAnotherPage) {
        System.out.println("calling playPreviewBgm, fromAnotherPage: " + fromAnotherPage);
        Beatmap beatmap = OsuParser.getCurrentBeatmap();
        File tempDir = ResourceManager.getTempDirectory();
        File beatmapDir = new File(tempDir, String.valueOf(beatmap.getBeatmapSetId()));
        File audioFile = new File(beatmapDir, "audio.mp3");

        if (!audioFile.exists()) {
            System.err.println("BGM file not found: " + audioFile.getAbsolutePath());
            return;
        }

        String newHash = computeFileHash(audioFile);
        if (!fromAnotherPage) {
            if (isSameBgm(newHash)) {
                System.out.println("Same BGM content. Skipping playback.");
                return;
            }
            stopBgm();
        } else {
            if(newHash != null && currentBgmHash.equals(defaultBgmHash)) {
                System.out.println("From another page, Default BGM content. Stopping BGM.");
                stopBgm();
            }
            if (isSameBgm(newHash) && currentPlayer != null) {
                System.out.println("From another page, Same BGM content. Resuming BGM.");
                System.out.println("Current Player Status: " + currentPlayer.getStatus());
//                currentPlayer.play();
                if(currentPlayer.getCurrentTime().lessThan(currentPlayer.getTotalDuration())) currentPlayer.play();
                else {
                    currentBgmHash = null;
                    playPreviewBgm(false);
                    return;
                }

                loopFromPreviewTime();
                return;
            }
        }
        currentBgmHash = newHash;

        Media media = new Media(audioFile.toURI().toString());
        currentPlayer = new MediaPlayer(media);

        BeatmapSet beatmapSet = beatmap.getBeatmapSet();
        String audioPath = ResourceManager.getBeatmapSetAudioPath(beatmapSet.getBeatmapSetId());
        Song song = new Song(beatmapSet.getBeatmapSetId(), beatmapSet.getTitle(), beatmapSet.getArtist(), audioPath);
        PlaylistManager.getInstance().playSong(song);

        currentPlayer.setOnReady(() -> {
            Duration previewTime = new Duration(OsuParser.getPreviewTime());
            currentPlayer.seek(previewTime);

            currentPlayer.setOnEndOfMedia(() -> {
                currentPlayer.seek(previewTime);
                currentPlayer.play();
            });
        });

        currentPlayer.setAutoPlay(true);
        currentPlayer.setVolume(0.2);
    }

    public void prepareGameBgm() {
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

    public void playGameBgm() {
        if(currentPlayer != null) {
            System.out.println("Playing game BGM");
            currentPlayer.play();
        }
    }

    public void playDefaultBgm(File bgmFile) {
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

    public void playAudio(String audioPath) {
        File songFile = new File(audioPath);

        stopBgm();
        try {
            Media media = new Media(songFile.toURI().toString());
            currentPlayer = new MediaPlayer(media);
            currentPlayer.setAutoPlay(true);
            currentPlayer.setVolume(0.2);
        } catch (Exception e) {
            System.err.println("Failed to load BGM: " + songFile.getPath());
            e.printStackTrace();
        }
    }

    public void pauseBgm() {
        if (currentPlayer != null) {
            currentPlayer.pause();
        }
    }

    public void resumeBgm() {
        if (currentPlayer != null) {
            currentPlayer.play();
        }
    }

    public void stopBgm() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer.dispose();
            currentPlayer = null;
        }
    }

    public void setVolume(double volume) {
        if (currentPlayer != null) {
            currentPlayer.setVolume(volume);
        }
    }
}