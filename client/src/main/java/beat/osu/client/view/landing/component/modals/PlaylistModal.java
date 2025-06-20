package beat.osu.client.view.landing.component.modals;

import java.net.URL;

import beat.osu.client.Main;
import beat.osu.client.events.song.SongChangeEvent;
import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.interfaces.song.SongEventListener;
import beat.osu.client.model.Song;
import beat.osu.client.view.landing.component.ui.PlaylistContent;
import beat.osu.client.view.landing.component.ui.PlaylistItem;
import javafx.animation.FadeTransition;
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

public class PlaylistModal extends StackPane implements SongEventListener {

    private VBox contentContainer;
    private PlaylistContent playlistContent;
    private VBox playlistItemsContainer;
    @Getter
    private PlaylistItem selectedItem;

    public PlaylistModal() {
        super();
        initializeComponents();
        setupLayout();
        setupStyling();
        populatePlaylist();
        loadStyles();
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
        Label searchLabel = new Label("Type to search!");
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
        playlistItemsContainer = new VBox();
        playlistItemsContainer.getStyleClass().add("playlist-items-container");

        for (Song song : BgmManager.getInstance().getPlaylist()) {
            PlaylistItem playlistItem = new PlaylistItem(song);
            playlistItem.setSelectionCallback(this::onItemSelected);
            playlistItemsContainer.getChildren().add(playlistItem);
        }
        
        playlistContent.setContent(playlistItemsContainer);
    }

    private void onItemSelected(PlaylistItem newSelection) {
        if (selectedItem != null) {
            selectedItem.setSelected(false);
        }
        
        selectedItem = newSelection;
        selectedItem.setSelected(true);
    }

    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            this.setVisible(false);
        });

        fadeOut.play();
    }

    public void show() {
        this.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    @Override
    public void update(SongChangeEvent event) {
        Song eventSong = event.getSong();
        if (eventSong == null || playlistItemsContainer == null) return;
        
        // Get the current song index from BgmManager for more efficient lookup
        int currentIndex = BgmManager.getInstance().getCurrentSongIndex();
        int playlistSize = playlistItemsContainer.getChildren().size();
        
        System.out.println("DEBUG PlaylistModal.update:");
        System.out.println("  Event song: " + eventSong.getArtist() + " - " + eventSong.getTitle() + " (ID: " + eventSong.getId() + ")");
        System.out.println("  Current index from BgmManager: " + currentIndex);
        System.out.println("  Playlist container size: " + playlistSize);
        
        // If we have a valid index, use it for direct access
        if (currentIndex >= 0 && currentIndex < playlistSize) {
            Object node = playlistItemsContainer.getChildren().get(currentIndex);
            if (node instanceof PlaylistItem) {
                PlaylistItem playlistItem = (PlaylistItem) node;
                Song itemSong = playlistItem.getSong();
                System.out.println("  Song at index " + currentIndex + ": " + itemSong.getArtist() + " - " + itemSong.getTitle() + " (ID: " + itemSong.getId() + ")");
                
                if (itemSong.getId() == eventSong.getId()) {
                    System.out.println("  ✓ Direct access MATCH! Selecting item at index " + currentIndex);
                    onItemSelected(playlistItem);
                    return;
                } else {
                    System.out.println("  ✗ Direct access MISMATCH! Expected ID " + eventSong.getId() + " but found " + itemSong.getId());
                }
            }
        } else {
            System.out.println("  ✗ Invalid index: " + currentIndex + " (size: " + playlistSize + ")");
        }
        
        System.out.println("  Falling back to full search...");
        // Fallback to searching through all items if direct access fails
        for (Object node : playlistItemsContainer.getChildren()) {
            if (node instanceof PlaylistItem) {
                PlaylistItem playlistItem = (PlaylistItem) node;
                Song itemSong = playlistItem.getSong();
                
                if (itemSong != null && eventSong.getId() == itemSong.getId()) {
                    System.out.println("  ✓ Fallback search found match!");
                    onItemSelected(playlistItem);
                    break;
                }
            }
        }
    }
}
