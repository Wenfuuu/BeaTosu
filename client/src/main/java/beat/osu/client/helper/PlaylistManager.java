package beat.osu.client.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import beat.osu.client.events.song.SongChangeEvent;
import beat.osu.client.interfaces.song.SongEventListener;
import beat.osu.client.interfaces.song.SongEventPublisher;
import beat.osu.client.model.Song;
import beat.osu.client.utils.BeatmapUtils;
import lombok.Getter;

public class PlaylistManager implements SongEventPublisher {
    private static volatile PlaylistManager instance;

    private List<Song> fullPlaylist;
    private List<Song> filteredPlaylist;
    private String currentFilter = "";
    private boolean isFiltered = false;
    @Getter
    private int currentSongIndex = -1;
    @Getter
    private Song currentSong;
    
    private final ArrayList<SongEventListener> listeners;

    private PlaylistManager() {
        fullPlaylist = new ArrayList<>();
        filteredPlaylist = new ArrayList<>();
        listeners = new ArrayList<>();
        initializePlaylist();
    }

    public static PlaylistManager getInstance() {
        if (instance == null) {
            synchronized (PlaylistManager.class) {
                if (instance == null) {
                    instance = new PlaylistManager();
                }
            }
        }
        return instance;
    }

    private void initializePlaylist() {
        try {
            fullPlaylist.clear();
            fullPlaylist.addAll(BeatmapUtils.getBeatmapSongs());
            System.out.println("Initialized playlist with " + fullPlaylist.size() + " songs");
        } catch (Exception e) {
            System.err.println("Failed to initialize playlist: " + e.getMessage());
        }
    }

    public void refreshPlaylist() {
        initializePlaylist();
        if (currentSongIndex >= getCurrentPlaylist().size()) {
            currentSongIndex = -1;
        }
    }

    public List<Song> getCurrentPlaylist() {
        return isFiltered ? filteredPlaylist : fullPlaylist;
    }

    public void applyFilter(String query) {
        if (query == null || query.trim().isEmpty()) {
            clearFilter();
        } else {
            this.currentFilter = query.toLowerCase().trim();
            this.isFiltered = true;
            this.filteredPlaylist = fullPlaylist.stream()
                .filter(song -> matchesQuery(song, this.currentFilter))
                .collect(Collectors.toList());
        }
    }

    public void clearFilter() {
        this.currentFilter = "";
        this.isFiltered = false;
        this.filteredPlaylist.clear();
    }

    private boolean matchesQuery(Song song, String query) {
        return song.getTitle().toLowerCase().contains(query) || 
               song.getArtist().toLowerCase().contains(query);
    }

    public Song getNextSong(Song currentSong) {
        List<Song> playlist = getCurrentPlaylist();
        return getNextSongInList(currentSong, playlist);
    }

    public Song getPreviousSong(Song currentSong) {
        List<Song> playlist = getCurrentPlaylist();
        return getPreviousSongInList(currentSong, playlist);
    }

    private Song getNextSongInList(Song currentSong, List<Song> playlist) {
        if (playlist.isEmpty()) {
            return null;
        }

        if (currentSong == null) {
            return playlist.get(0);
        }

        int currentIndex = findSongIndex(currentSong, playlist);
        if (currentIndex == -1) {
            return playlist.get(0);
        }

        int nextIndex = (currentIndex + 1) % playlist.size();
        return playlist.get(nextIndex);
    }

    private Song getPreviousSongInList(Song currentSong, List<Song> playlist) {
        if (playlist.isEmpty()) {
            return null;
        }

        if (currentSong == null) {
            return playlist.get(playlist.size() - 1);
        }

        int currentIndex = findSongIndex(currentSong, playlist);
        if (currentIndex == -1) {
            return playlist.get(playlist.size() - 1);
        }

        int previousIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        return playlist.get(previousIndex);
    }

    private int findSongIndex(Song song, List<Song> playlist) {
        for (int i = 0; i < playlist.size(); i++) {
            if (playlist.get(i).getId() == song.getId()) {
                return i;
            }
        }
        return -1;
    }

    public void updateCurrentSongIndex(Song song) {
        this.currentSongIndex = findSongIndex(song, fullPlaylist);
    }

    public void playNextSong() {
        Song nextSong = getNextSong(currentSong);
        if (nextSong != null) {
            BgmManager.getInstance().playAudio(nextSong.getAudioPath());
            setCurrentSong(nextSong);
        } else {
            System.err.println("No next song available in playlist");
        }
    }

    public void playPreviousSong() {
        Song previousSong = getPreviousSong(currentSong);
        if (previousSong != null) {
            BgmManager.getInstance().playAudio(previousSong.getAudioPath());
            setCurrentSong(previousSong);
        } else {
            System.err.println("No previous song available in playlist");
        }
    }

    public void playRandomSong() {
        List<Song> playlist = getCurrentPlaylist();
        if (playlist.isEmpty()) {
            System.err.println("No songs available in playlist");
            return;
        }
        
        Random random = new Random();
        int randomIndex = random.nextInt(playlist.size());
        Song randomSong = playlist.get(randomIndex);
        
        playSong(randomSong);
    }

    public void playSong(Song song) {
        setCurrentSong(song);
        BgmManager.getInstance().playAudio(song.getAudioPath());
        notifyListeners(new SongChangeEvent(song));
    }

    public void setCurrentSongForPreview(Song song) {
        setCurrentSong(song);
        notifyListeners(new SongChangeEvent(song));
    }

    private void setCurrentSong(Song song) {
        this.currentSong = song;
        updateCurrentSongIndex(song);
        notifyListeners(new SongChangeEvent(song));
    }

    public boolean isNoSongPlaying() {
        return currentSong == null;
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
