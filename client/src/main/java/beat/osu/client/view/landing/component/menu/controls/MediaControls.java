package beat.osu.client.view.landing.component.menu.controls;

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
    private Button optionsButton;
    private Button playlistButton;

    public MediaControls() {
        super(15); // Spacing between controls
        this.getStyleClass().add("controls-bar");

        // Initialize components
        initializeComponents();

        // Set layout
        setupLayout();
    }

    private void initializeComponents() {
        prevButton = createButton("⏮");
        playButton = createButton("▶");
        pauseButton = createButton("⏸");
        stopButton = createButton("⏹");
        nextButton = createButton("⏭");
        optionsButton = createButton("⚙");
        playlistButton = createButton("≡");
    }

    private void setupLayout() {
        this.getChildren().addAll(prevButton, playButton, pauseButton, stopButton, nextButton, optionsButton, playlistButton);
    }

    private Button createButton(String symbol) {
        Button button = new Button(symbol);
        button.getStyleClass().add("media-button");
        return button;
    }
}
