package beat.osu.client.view.landing.component.ui;

import java.net.URL;

import beat.osu.client.events.song.SongChangeEvent;
import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.interfaces.song.SongEventListener;
import beat.osu.client.view.landing.component.controls.MediaControls;
import beat.osu.client.view.landing.component.modals.PlaylistModal;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

@Getter
public class Jukebox extends StackPane implements SongEventListener {

    private final PlaylistModal playlistModal;

    private CurrentSongBox currentSongBox;
    private MediaControls mediaControls;

    private VBox contentBox;

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
        this.currentSongBox = new CurrentSongBox("Nekodex - Circles!");
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
        this.contentBox = new VBox(12);
        contentBox.getStyleClass().add("jukebox-content");
        contentBox.getChildren().addAll(currentSongBox, mediaControls);
        this.getChildren().addAll(contentBox, playlistModal);
        
        animateCurrentSongBoxIn(currentSongBox);
    }

    private void setupEventHandlers() {
        mediaControls.getPlaylistButton().setOnAction(event -> {
            if (playlistModal.isVisible()) {
                playlistModal.hide();
            } else {
                playlistModal.show();
            }
        });

        mediaControls.getPlayButton().setOnAction(e -> BgmManager.getInstance().resumeBgm());
        mediaControls.getPauseButton().setOnAction(e -> BgmManager.getInstance().pauseBgm());
        mediaControls.getStopButton().setOnAction(e -> BgmManager.getInstance().stopBgm());
    }

    private void animateCurrentSongBoxIn(CurrentSongBox songBox) {
        int translateLength = songBox.getCurrentSongLabel().getText().length() * 10;

        songBox.setTranslateX(translateLength);
        songBox.setOpacity(0);

        Timeline slideIn = new Timeline();
        slideIn.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO,
                new KeyValue(songBox.translateXProperty(), translateLength),
                new KeyValue(songBox.opacityProperty(), 0)
            ),
            new KeyFrame(Duration.millis(500),
                new KeyValue(songBox.translateXProperty(), 0),
                new KeyValue(songBox.opacityProperty(), 1)
            )
        );
        slideIn.play();
    }

    @Override
    public void update(SongChangeEvent event) {
        this.contentBox.getChildren().remove(currentSongBox);
        this.currentSongBox = new CurrentSongBox(event.getSong().getArtist() + " - " + event.getSong().getTitle());
        this.contentBox.getChildren().add(0, currentSongBox);

        animateCurrentSongBoxIn(currentSongBox);
    }
}
