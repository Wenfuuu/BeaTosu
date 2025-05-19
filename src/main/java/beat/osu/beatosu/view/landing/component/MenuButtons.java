package beat.osu.beatosu.view.landing.component;

import beat.osu.beatosu.helper.CssManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;

public class MenuButtons extends VBox {

    private Button menuPlayButton;
    private Button menuOptionButton;
    private Button menuExitButton;

    public MenuButtons() {
        super(20); // Spacing between buttons
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
        menuPlayButton = createMenuButton("Play");
        menuOptionButton = createMenuButton("Options");
        menuExitButton = createMenuButton("Exit");

        menuPlayButton.setAlignment(Pos.CENTER);
        menuOptionButton.setAlignment(Pos.CENTER);
        menuExitButton.setAlignment(Pos.CENTER);
    }

    private void setupLayout() {
        this.getChildren().addAll(menuPlayButton, menuOptionButton, menuExitButton);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getCssURL("MenuButtons.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-button");
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
