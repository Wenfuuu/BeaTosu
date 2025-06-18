package beat.osu.client.view.landing.component.ui;

import beat.osu.client.helper.CssManager;
import beat.osu.client.view.landing.component.controls.MediaControls;
import beat.osu.client.view.landing.component.modals.PlaylistModal;
import javafx.scene.layout.StackPane;
import lombok.Getter;

import java.net.URL;

@Getter
public class Jukebox extends StackPane {

    private PlaylistModal playlistModal;
    private MediaControls mediaControls;

    public Jukebox() {
        initializeComponents();
        loadStyles();
        setupStyles();
    }

    private void initializeComponents() {
        this.playlistModal = new PlaylistModal();
        this.mediaControls = new MediaControls();
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

    private void setupStyles() {

    }
}
