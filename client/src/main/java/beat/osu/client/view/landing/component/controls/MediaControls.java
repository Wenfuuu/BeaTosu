package beat.osu.client.view.landing.component.controls;

import java.net.URL;

import beat.osu.client.helper.CssManager;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import lombok.Getter;

@Getter
public class MediaControls extends HBox {

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
