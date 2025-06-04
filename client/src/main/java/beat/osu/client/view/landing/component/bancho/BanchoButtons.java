package beat.osu.client.view.landing.component.bancho;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import lombok.Getter;

@Getter
public class BanchoButtons extends HBox {

    private final ShowTickerButton showTickerButton;
    private final AutoHideButton autoHideButton;
    private final OnlineUsersButton onlineUsersButton;
    private final ChatToggleButton chatToggleButton;

    public BanchoButtons() {
        super();

        showTickerButton = new ShowTickerButton();
        autoHideButton = new AutoHideButton();
        chatToggleButton = new ChatToggleButton();
        onlineUsersButton = new OnlineUsersButton();

        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        this.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        this.setAlignment(Pos.BOTTOM_RIGHT);
        this.setSpacing(8);

        this.getChildren().addAll(showTickerButton, autoHideButton, onlineUsersButton, chatToggleButton);
    }

    public void toggleChat(OnlineUsersPanel onlineUsersPanel) {
        if (chatToggleButton.isChatVisible()) {
            onlineUsersPanel.hide();
            chatToggleButton.setShowIcon();
        } else {
            onlineUsersPanel.show();
            chatToggleButton.setHideIcon();
        }
    }

    public void toggleOnlineUsers() {
        if (onlineUsersButton.isOnlineUserShown()) {
            onlineUsersButton.setOnlineUsersHiddenIcon();
        } else {
            onlineUsersButton.setOnlineUsersShownIcon();
        }
    }

    public void toggleAutoHide() {
        if (autoHideButton.isAutoHideEnabled()) {
            autoHideButton.setAutoHideOffIcon();
        } else {
            autoHideButton.setAutoHideOnIcon();
        }
    }

    public void toggleTicker() {
        if (showTickerButton.isTickerShown()) {
            showTickerButton.setShowTickerOffIcon();
        } else {
            showTickerButton.setShowTickerOnIcon();
        }
    }
}
