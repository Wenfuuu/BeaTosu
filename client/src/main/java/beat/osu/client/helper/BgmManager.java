package beat.osu.client.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import beat.osu.client.events.song.SongChangeEvent;
import beat.osu.client.interfaces.song.SongEventListener;
import beat.osu.client.interfaces.song.SongEventPublisher;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BeatmapSet;
import beat.osu.client.model.Song;
import beat.osu.client.utils.BeatmapUtils;
import beat.osu.client.utils.OsuParser;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.Getter;

@Getter
public class BgmManager implements SongEventPublisher {
    private static volatile BgmManager instance;

    private String currentBgmHash = null;
    private String defaultBgmHash;
    private MediaPlayer currentPlayer;
    private final ArrayList<SongEventListener> listeners;

    @Getter
    private List<Song> playlist;
    @Getter
    private int currentSongIndex = -1;
    @Getter
    private Song currentSong;

    private BgmManager() {
        listeners = new ArrayList<>();
        playlist = new ArrayList<>();
        initializePlaylist();
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
        playSong(song);

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

    private void initializePlaylist() {
        try {
            playlist.clear();
            playlist.addAll(BeatmapUtils.getBeatmapSongs());
            System.out.println("Initialized playlist with " + playlist.size() + " songs");
        } catch (Exception e) {
            System.err.println("Failed to initialize playlist: " + e.getMessage());
        }
    }

    public void refreshPlaylist() {
        initializePlaylist();
        if (currentSongIndex >= playlist.size()) {
            currentSongIndex = -1;
            currentSong = null;
        }
    }

    public void playNextSong() {
        if (playlist.isEmpty()) {
            refreshPlaylist();
            if (playlist.isEmpty()) {
                System.err.println("No songs available in playlist");
                return;
            }
        }

        currentSongIndex = (currentSongIndex + 1) % playlist.size();
        currentSong = playlist.get(currentSongIndex);
        playSong(currentSong);
    }

    public void playPreviousSong() {
        if (playlist.isEmpty()) {
            refreshPlaylist();
            if (playlist.isEmpty()) {
                System.err.println("No songs available in playlist");
                return;
            }
        }

        currentSongIndex = (currentSongIndex - 1 + playlist.size()) % playlist.size();
        currentSong = playlist.get(currentSongIndex);
        playSong(currentSong);
    }

    public void playNextSongFromFiltered(List<Song> filteredSongs) {
        Song nextSong = getNextSongFromFiltered(filteredSongs);
        if (nextSong != null) {
            playSong(nextSong);
        }
    }

    public void playPreviousSongFromFiltered(List<Song> filteredSongs) {
        Song previousSong = getPreviousSongFromFiltered(filteredSongs);
        if (previousSong != null) {
            playSong(previousSong);
        }
    }

    private Song getNextSongFromFiltered(List<Song> filteredSongs) {
        if (currentSong == null) {
            return filteredSongs.isEmpty() ? null : filteredSongs.get(0);
        }

        int currentFilteredIndex = -1;
        for (int i = 0; i < filteredSongs.size(); i++) {
            if (filteredSongs.get(i).getId() == currentSong.getId()) {
                currentFilteredIndex = i;
                break;
            }
        }

        if (currentFilteredIndex == -1) {
            return filteredSongs.isEmpty() ? null : filteredSongs.get(0);
        }

        int nextIndex = (currentFilteredIndex + 1) % filteredSongs.size();
        return filteredSongs.get(nextIndex);
    }

    private Song getPreviousSongFromFiltered(List<Song> filteredSongs) {
        if (currentSong == null) {
            return filteredSongs.isEmpty() ? null : filteredSongs.get(filteredSongs.size() - 1);
        }

        int currentFilteredIndex = -1;
        for (int i = 0; i < filteredSongs.size(); i++) {
            if (filteredSongs.get(i).getId() == currentSong.getId()) {
                currentFilteredIndex = i;
                break;
            }
        }

        if (currentFilteredIndex == -1) {
            return filteredSongs.isEmpty() ? null : filteredSongs.get(filteredSongs.size() - 1);
        }

        int previousIndex = (currentFilteredIndex - 1 + filteredSongs.size()) % filteredSongs.size();
        return filteredSongs.get(previousIndex);
    }

    public void playSong(Song song) {
        File songFile = new File(song.getAudioPath());

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

        currentSong = song;
        for (int i = 0; i < playlist.size(); i++) {
            if (playlist.get(i).getId() == song.getId()) {
                currentSongIndex = i;
                break;
            }
        }

        notifyListeners(new SongChangeEvent(song));
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

    public boolean isPlaying() {
        return currentPlayer != null && currentPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    @Override
    public void addListener(SongEventListener songEventListener) {
        listeners.add(songEventListener);
    }

    @Override
    public void removeListener(SongEventListener songEventListener) {
        listeners.remove(songEventListener);
    }

    @Override
    public void notifyListeners(SongChangeEvent event) {
        for (SongEventListener listener : listeners) {
            listener.update(event);
        }
    }
}
