package beat.osu.client.view.shared.jukebox;

import java.net.URL;

import beat.osu.client.events.song.SongChangeEvent;
import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.PlaylistManager;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.interfaces.song.SongEventListener;
import beat.osu.client.model.Song;
import beat.osu.client.view.shared.jukebox.components.MediaControls;
import beat.osu.client.view.shared.jukebox.modals.PlaylistModal;
import beat.osu.client.view.shared.jukebox.cards.CurrentSongCard;
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

    private CurrentSongCard currentSongCard;
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
        if (PlaylistManager.getInstance().getCurrentSong() != null) {
            Song song = PlaylistManager.getInstance().getCurrentSong();
            this.currentSongCard = new CurrentSongCard(song.getArtist() + " - " + song.getTitle());
        } else {
            this.currentSongCard = new CurrentSongCard("Nekodex - Circles!");
        }

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

        URL cssUrl = CssManager.getSharedCssURL("Jukebox.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }
    }

    private void setupLayout() {
        this.contentBox = new VBox(12);
        contentBox.getStyleClass().add("jukebox-content");
        contentBox.getChildren().addAll(currentSongCard, mediaControls);
        this.getChildren().addAll(contentBox, playlistModal);
        
        animateCurrentSongCardIn(currentSongCard);
    }

    private void setupEventHandlers() {
        mediaControls.getPlayButton().setOnMouseEntered(e -> SfxManager.playSfx("menuhover.wav"));
        mediaControls.getPlayButton().setOnAction(e -> {
            SfxManager.playSfx("menuhit.wav");
            BgmManager.getInstance().resumeBgm();
        });

        mediaControls.getPauseButton().setOnMouseEntered(e -> SfxManager.playSfx("menuhover.wav"));
        mediaControls.getPauseButton().setOnAction(e -> {
            SfxManager.playSfx("menuhit.wav");
            BgmManager.getInstance().pauseBgm();
        });

        mediaControls.getStopButton().setOnMouseEntered(e -> SfxManager.playSfx("menuhover.wav"));
        mediaControls.getStopButton().setOnAction(e -> {
            SfxManager.playSfx("menuhit.wav");
            BgmManager.getInstance().stopBgm();
        });

        mediaControls.getNextButton().setOnMouseEntered(e -> SfxManager.playSfx("menuhover.wav"));
        mediaControls.getNextButton().setOnAction(e -> {
            SfxManager.playSfx("menuhit.wav");
            playlistModal.playNextSong();
        });

        mediaControls.getPrevButton().setOnMouseEntered(e -> SfxManager.playSfx("menuhover.wav"));
        mediaControls.getPrevButton().setOnAction(e -> {
            SfxManager.playSfx("menuhit.wav");
            playlistModal.playPreviousSong();
        });

        mediaControls.getPlaylistButton().setOnMouseEntered(e -> SfxManager.playSfx("menuhover.wav"));
    }

    private void animateCurrentSongCardIn(CurrentSongCard songBox) {
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
        this.contentBox.getChildren().remove(currentSongCard);
        this.currentSongCard = new CurrentSongCard(event.getSong().getArtist() + " - " + event.getSong().getTitle());
        this.contentBox.getChildren().add(0, currentSongCard);

        animateCurrentSongCardIn(currentSongCard);
    }
}
