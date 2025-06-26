package beat.osu.client.view.shared.jukebox.components;

import java.net.URL;

import beat.osu.client.helper.CssManager;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
        super(20);

        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            this.getStylesheets().add(globalCssUrl.toExternalForm());
        } else {
            System.err.println("index.css file not found!");
        }

        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        prevButton = createImageButton("jukebox-prev.png");
        playButton = createImageButton("jukebox-play.png");
        pauseButton = createImageButton("jukebox-pause.png");
        stopButton = createImageButton("jukebox-stop.png");
        nextButton = createImageButton("jukebox-next.png");
        playlistButton = createImageButton("jukebox-more.png");
    }

    private void setupLayout() {
        this.getChildren().addAll(prevButton, playButton, pauseButton, stopButton, nextButton, playlistButton);
    }

    private Button createImageButton(String imageName) {
        Button button = new Button();
        button.getStyleClass().add("media-button");
        
        try {
            String imagePath = "/assets/buttons/jukebox/" + imageName;
            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(27);
                imageView.setFitHeight(27);
                imageView.setPreserveRatio(true);
                button.setGraphic(imageView);
            } else {
                System.err.println("Image not found: " + imagePath);
                button.setText("?");
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + imageName + " - " + e.getMessage());
            button.setText("?");
        }
        
        return button;
    }
}
