package beat.osu.client.view.landing.component.modals;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import beat.osu.client.Main;
import beat.osu.client.events.song.SongChangeEvent;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.interfaces.song.SongEventListener;
import beat.osu.client.interfaces.song.SongEventPublisher;
import beat.osu.client.model.Song;
import beat.osu.client.utils.BeatmapUtils;
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

public class PlaylistModal extends StackPane implements SongEventPublisher {

    private VBox contentContainer;
    private PlaylistContent playlistContent;
    @Getter
    private PlaylistItem selectedItem;

    private List<SongEventListener> listeners;

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
        this.listeners = new ArrayList<>();
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
        VBox playlistItemsContainer = new VBox();
        playlistItemsContainer.getStyleClass().add("playlist-items-container");

        for (Song song : BeatmapUtils.getBeatmapSongs()) {
            PlaylistItem playlistItem = new PlaylistItem(song);
            
            playlistItem.setSelectionCallback(this::onItemSelected);
            
            playlistItemsContainer.getChildren().add(playlistItem);
        }
        
        playlistContent.setContent(playlistItemsContainer);
    }

    private void onItemSelected(PlaylistItem newSelection) {
        if (selectedItem != null && selectedItem != newSelection) {
            selectedItem.setSelected(false);
        }
        
        selectedItem = newSelection;
        selectedItem.setSelected(true);

        notifyListeners(new SongChangeEvent(newSelection.getSong()));
    }

    public void refreshPlaylist() {
        populatePlaylist();
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
