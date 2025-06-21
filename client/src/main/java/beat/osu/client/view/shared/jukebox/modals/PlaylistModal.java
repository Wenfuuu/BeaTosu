package beat.osu.client.view.shared.jukebox.modals;

import java.net.URL;
import java.util.List;

import beat.osu.client.Main;
import beat.osu.client.events.song.SongChangeEvent;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.InputManager;
import beat.osu.client.helper.PlaylistManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.interfaces.song.SongEventListener;
import beat.osu.client.model.Song;
import beat.osu.client.view.shared.jukebox.components.PlaylistContent;
import beat.osu.client.view.shared.jukebox.components.PlaylistItem;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

public class PlaylistModal extends StackPane implements SongEventListener {

    private VBox contentContainer;
    private PlaylistContent playlistContent;
    private VBox playlistItemsContainer;
    @Getter
    private PlaylistItem selectedItem;
    private Label searchLabel;
    @Setter
    private InputManager inputManager;
    private Timeline searchUpdateTimeline;
    private String lastSearchQuery = "";
    private PlaylistManager playlistManager;

    public PlaylistModal() {
        super();
        playlistManager = PlaylistManager.getInstance();
        initializeComponents();
        setupLayout();
        setupStyling();
        populatePlaylist();
        loadStyles();
        setupSearchUpdater();
    }
    
    private void setupSearchUpdater() {
        searchUpdateTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> updateSearch()));
        searchUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    private void updateSearch() {
        if (inputManager == null) return;
        
        String currentQuery = inputManager.getTypedChars().toLowerCase().trim();
        
        if (!currentQuery.equals(lastSearchQuery)) {
            lastSearchQuery = currentQuery;
            
            playlistManager.applyFilter(currentQuery);
            
            if (currentQuery.isEmpty()) {
                searchLabel.setText("Type to search!");
            } else {
                searchLabel.setText(currentQuery);
            }
            
            populatePlaylist();
            updateSelectedItem();
        }
    }
    
    private void updateSelectedItem() {
        Song currentSong = PlaylistManager.getInstance().getCurrentSong();
        if (currentSong == null) return;
        
        List<Song> displayedSongs = playlistManager.getCurrentPlaylist();
        
        for (int i = 0; i < displayedSongs.size(); i++) {
            if (displayedSongs.get(i).getId() == currentSong.getId()) {
                if (i < playlistItemsContainer.getChildren().size()) {
                    Object node = playlistItemsContainer.getChildren().get(i);
                    if (node instanceof PlaylistItem) {
                        PlaylistItem playlistItem = (PlaylistItem) node;
                        onItemSelected(playlistItem);
                    }
                }
                break;
            }
        }
    }
    
    private List<Song> getCurrentDisplayedSongs() {
        return playlistManager.getCurrentPlaylist();
    }
    
    private void populatePlaylistWithSongs(List<Song> songs) {
        if (playlistItemsContainer == null) {
            playlistItemsContainer = new VBox();
            playlistItemsContainer.getStyleClass().add("playlist-items-container");
            playlistContent.setContent(playlistItemsContainer);
        }
        
        playlistItemsContainer.getChildren().clear();
        
        for (Song song : songs) {
            PlaylistItem playlistItem = new PlaylistItem(song);
            playlistItem.setSelectionCallback(this::onItemSelected);
            playlistItemsContainer.getChildren().add(playlistItem);
        }
    }
    
    private void loadStyles() {
        URL modalCssUrl = CssManager.getLandingCssURL("PlaylistModal.css");
        if (modalCssUrl != null) {
            this.getStylesheets().add(modalCssUrl.toExternalForm());
        } else {
            System.err.println("PlaylistModal.css file not found!");
        }
        
        URL itemCssUrl = CssManager.getLandingCssURL("PlaylistItem.css");
        if (itemCssUrl != null) {
            this.getStylesheets().add(itemCssUrl.toExternalForm());
        } else {
            System.err.println("PlaylistItem.css file not found!");
        }
    }

    private void initializeComponents() {
        contentContainer = new VBox();
        playlistContent = new PlaylistContent();
    }
    
    private void setupLayout() {
        Label titleLabel = new Label("Jump To...");
        titleLabel.setFont(new Font("Aller Light", ScreenManager.SCREEN_HEIGHT / 20));

        HBox searchArea = new HBox();
        searchLabel = new Label("Type to search!");
        searchLabel.getStyleClass().add("search-label");
        
        ImageView searchIcon = null;
        String searchIconPath = "/assets/images/search-icon.png";
        URL searchIconUrl = Main.class.getResource(searchIconPath);
        
        if (searchIconUrl != null) {
            Image searchImage = new Image(searchIconUrl.toExternalForm());
            searchIcon = new ImageView(searchImage);
            searchIcon.setFitHeight(32);
            searchIcon.setFitWidth(32);
            searchIcon.setPreserveRatio(true);
            searchIcon.setStyle("-fx-padding: 0 0 0 8;");
            searchArea.getChildren().addAll(searchLabel, searchIcon);
        } else {
            searchArea.getChildren().add(searchLabel);
        }
        
        searchArea.setAlignment(Pos.CENTER_RIGHT);
        searchArea.setPadding(new Insets(0, 20, 10, 20));
        
        contentContainer.getChildren().addAll(titleLabel, searchArea, playlistContent);
        contentContainer.setAlignment(Pos.TOP_LEFT);
        
        this.getChildren().add(contentContainer);
        this.setAlignment(Pos.CENTER);
    }
    
    private void setupStyling() {
        this.getStyleClass().add("playlist-modal");
        contentContainer.getStyleClass().add("playlist-modal-content");
    }
    
    private void populatePlaylist() {
        populatePlaylistWithSongs(playlistManager.getCurrentPlaylist());
    }

    private void onItemSelected(PlaylistItem newSelection) {
        if (selectedItem != null) {
            selectedItem.setSelected(false);
        }
        
        selectedItem = newSelection;
        selectedItem.setSelected(true);
    }

    public void hide() {
        if (searchUpdateTimeline != null) {
            searchUpdateTimeline.stop();
        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            this.setVisible(false);
        });

        fadeOut.play();
    }    public void show() {
        this.setVisible(true);
        
        if (inputManager != null) {
            inputManager.clearTypedChars();
            lastSearchQuery = "";
            searchLabel.setText("Type to search!");
            playlistManager.clearFilter();
            populatePlaylist();

            Song song = PlaylistManager.getInstance().getCurrentSong();
            SongChangeEvent event = new SongChangeEvent(song);
            update(event);
        }

        if (searchUpdateTimeline != null) {
            searchUpdateTimeline.play();
        }

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    @Override
    public void update(SongChangeEvent event) {
        Song eventSong = event.getSong();
        if (eventSong == null || playlistItemsContainer == null) return;
        
        updateSelectedItem();
    }

    public void playNextSong() {
        PlaylistManager.getInstance().playNextSong();
    }
    
    public void playPreviousSong() {
        PlaylistManager.getInstance().playPreviousSong();
    }
}
