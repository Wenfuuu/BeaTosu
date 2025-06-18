package beat.osu.client.view.landing.component.controls;

import beat.osu.client.helper.CssManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.net.URL;

@Getter
public class MediaControls extends VBox {

    private Label currentSongTitle;
    private HBox mediaButtons;

    private Button prevButton;
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private Button nextButton;
    private Button playlistButton;

    public MediaControls() {
        super(8);

        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            this.getStylesheets().add(globalCssUrl.toExternalForm());
        } else {
            System.err.println("index.css file not found!");
        }

        URL cssUrl = CssManager.getLandingCssURL("Jukebox.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("LoginModal.css file not found!");
        }

        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        prevButton = createButton("⏮");
        playButton = createButton("▶");
        pauseButton = createButton("⏸");
        stopButton = createButton("⏹");
        nextButton = createButton("⏭");
        playlistButton = createButton("≡");
    }

    private void setupLayout() {
        this.getChildren().addAll(prevButton, playButton, pauseButton, stopButton, nextButton, playlistButton);
    }

    private Button createButton(String symbol) {
        Button button = new Button(symbol);
        button.getStyleClass().add("media-button");
        return button;
    }
}
