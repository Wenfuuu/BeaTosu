package beat.osu.client.view.lobby.component.layout;

import beat.osu.client.enums.SfxType;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.SfxManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import lombok.Getter;

import java.net.URL;

@Getter
public class NavigationBar extends HBox {

    private Button backButton;
    private Button newGameButton;
    private Button quickJoinButton;

    public NavigationBar() {
        initializeComponents();
        setLayout();
        setupStyling();
    }

    private void initializeComponents() {
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("navigation-bar");

        this.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.06);
        this.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.06);
        this.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.06);

        backButton = new Button("Back to Menu");
        newGameButton = new Button("New Game");
        quickJoinButton = new Button("Quick Join");

        backButton.setOnMouseEntered(e -> SfxManager.playMenuSfx(SfxType.MENU_HOVER));
        newGameButton.setOnMouseEntered(e -> SfxManager.playMenuSfx(SfxType.MENU_HOVER));
        quickJoinButton.setOnMouseEntered(e -> SfxManager.playMenuSfx(SfxType.MENU_HOVER));

        backButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.22);
        newGameButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.22);
        quickJoinButton.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.22);

        backButton.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.04);
        newGameButton.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.04);
        quickJoinButton.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.04);

        backButton.getStyleClass().addAll("button", "back-button");
        newGameButton.getStyleClass().addAll("button", "new-game-button");
        quickJoinButton.getStyleClass().addAll("button", "quick-join-button");
    }

    private void setLayout() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(30);
        this.setPadding(new Insets(20, 240, 20, 240));

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