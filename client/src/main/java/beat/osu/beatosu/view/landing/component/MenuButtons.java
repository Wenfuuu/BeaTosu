package beat.osu.beatosu.view.landing.component;

import beat.osu.beatosu.helper.CssManager;
import beat.osu.beatosu.helper.ScreenManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;

public class MenuButtons extends VBox {

    private Button menuPlayButton;
    private Button menuOptionButton;
    private Button menuExitButton;

    public MenuButtons() {
        super(20);
        this.getStyleClass().add("menu-box");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setMaxWidth(Region.USE_PREF_SIZE);

        // Initialize components
        initializeComponents();

        // Set layout
        setupLayout();

        // Load CSS
        loadStyles();
    }

    private void initializeComponents() {
        menuPlayButton = createMenuButton("play.png");
        menuOptionButton = createMenuButton("options.png");
        menuExitButton = createMenuButton("exit.png");

        menuPlayButton.setAlignment(Pos.CENTER);
        menuOptionButton.setAlignment(Pos.CENTER);
        menuExitButton.setAlignment(Pos.CENTER);
    }

    private void setupLayout() {
        this.getChildren().addAll(menuPlayButton, menuOptionButton, menuExitButton);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getLandingCssURL("a.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private Button createMenuButton(String imageName) {
        Button button = new Button();
        try {
            String imagePath = "/assets/buttons/" + imageName;
            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl == null) {
                System.err.println("Image not found: " + imagePath);
                button.setText(imageName.substring(0, imageName.lastIndexOf('.')));
            } else {
                Image image = new Image(imageUrl.toExternalForm());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(ScreenManager.SCREEN_HEIGHT / 1.67);
                imageView.setPreserveRatio(true);
                button.setGraphic(imageView);
                button.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
            }
        } catch (Exception e) {
            System.err.println("Error loading image " + imageName + ": " + e.getMessage());
            button.setText(imageName.substring(0, imageName.lastIndexOf('.')));
        }
        button.getStyleClass().add("menu-button"); // Keep existing style class for other potential styling
        return button;
    }

    // Getters for the buttons
    public Button getPlayButton() {
        return menuPlayButton;
    }

    public Button getOptionButton() {
        return menuOptionButton;
    }

    public Button getExitButton() {
        return menuExitButton;
    }
}
