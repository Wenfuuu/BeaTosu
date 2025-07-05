package beat.osu.client.view.shared.bancho.buttons;

import beat.osu.client.enums.SfxType;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.view.shared.bancho.panels.ChatPanel;
import beat.osu.client.view.shared.bancho.panels.OnlineUsersPanel;
import beat.osu.client.view.landing.component.layout.BottomBar;
import beat.osu.client.view.landing.component.layout.TopBar;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import lombok.Getter;

@Getter
public class BanchoButtons extends HBox {

    private final OnlineUsersButton onlineUsersButton;
    private final ChatToggleButton chatToggleButton;

    public BanchoButtons() {
        super();

        chatToggleButton = new ChatToggleButton();
        onlineUsersButton = new OnlineUsersButton();

        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        this.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        this.setAlignment(Pos.BOTTOM_RIGHT);
        this.setSpacing(8);

        this.getChildren().addAll(onlineUsersButton, chatToggleButton);

        handleEvents();
    }

    private void handleEvents() {
        onlineUsersButton.setOnMouseEntered(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_HOVER);
        });

        chatToggleButton.setOnMouseEntered(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_HOVER);
        });
    }

    public void toggleChat(ChatPanel chatPanel, BottomBar bottomBar, OnlineUsersPanel onlineUsersPanel, TopBar topBar) {

    }

    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            this.setVisible(false);
        });

        fadeOut.play();
    }

    public void show() {
        this.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
}
