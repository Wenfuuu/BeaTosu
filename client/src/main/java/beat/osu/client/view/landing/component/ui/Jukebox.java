package beat.osu.client.view.landing.component.ui;

import java.net.URL;

import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.view.landing.component.controls.MediaControls;
import beat.osu.client.view.landing.component.modals.PlaylistModal;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

@Getter
public class Jukebox extends StackPane {

    private final PlaylistModal playlistModal;

    private CurrentSongBox currentSongBox;
    private MediaControls mediaControls;

    public Jukebox(PlaylistModal playlistModal) {
        this.playlistModal = playlistModal;
        this.getStyleClass().add("jukebox");

        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        this.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        initializeComponents();
        loadStyles();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        this.currentSongBox = new CurrentSongBox("Ayakura Mei - Romantic Fall");
        this.mediaControls = new MediaControls();
        this.mediaControls.getStyleClass().add("media-controls");
    }

    private void loadStyles() {
        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            this.getStylesheets().add(globalCssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }

        URL cssUrl = CssManager.getLandingCssURL("Jukebox.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }
    }

    private void setupLayout() {
        VBox contentBox = new VBox(16);
        contentBox.getStyleClass().add("jukebox-content");
        contentBox.getChildren().addAll(currentSongBox, mediaControls);
        this.getChildren().addAll(contentBox, playlistModal);
    }

    private void setupEventHandlers() {
        mediaControls.getPlaylistButton().setOnAction(event -> {
            playlistModal.setVisible(!playlistModal.isVisible());
        });

        mediaControls.getPlayButton().setOnAction(e -> BgmManager.resumeBgm());
        mediaControls.getPauseButton().setOnAction(e -> BgmManager.pauseBgm());
        mediaControls.getStopButton().setOnAction(e -> BgmManager.stopBgm());
    }
}
