package beat.osu.client.view.lobby.component.layout;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.net.URL;

public class NavigationBar extends HBox {

    @Getter
    private Button backButton;

    @Getter
    private Button newGameButton;

    @Getter
    private Button quickJoinButton;

    public NavigationBar() {
        initializeComponents();
        setLayout();
        setupStyling();
    }

    private void initializeComponents() {
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("navigation-bar");

        backButton = new Button("Back to Menu");
        newGameButton = new Button("New Game");
        quickJoinButton = new Button("Quick Join");

        backButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.22);
        newGameButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.22);
        quickJoinButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.22);

        backButton.getStyleClass().addAll("button", "back-button");
        newGameButton.getStyleClass().addAll("button", "new-game-button");
        quickJoinButton.getStyleClass().addAll("button", "quick-join-button");
    }

    private void setLayout() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(30);
        this.setPadding(new Insets(0, 240, 0, 240));

        this.getChildren().addAll(backButton, newGameButton, quickJoinButton);
    }

    private void setupStyling() {
        try {
            URL cssUrl = CssManager.getLobbyCssURL("NavigationBar.css");
            if (cssUrl != null) {
                this.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Could not load NavigationBar CSS: " + e.getMessage());
        }
    }
}