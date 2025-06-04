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

    public void toggleOnlineUsers(OnlineUsersPanel onlineUsersPanel, ChatPanel chatPanel) {
        if (onlineUsersButton.isOnlineUserShown()) {
            onlineUsersPanel.hide();
            onlineUsersButton.setOnlineUsersHiddenIcon();
        } else {
            onlineUsersPanel.show();
            onlineUsersButton.setOnlineUsersShownIcon();

            chatPanel.show();
            chatToggleButton.setHideIcon();
        }
    }

    public void toggleChat(ChatPanel chatPanel) {
        if (chatToggleButton.isChatVisible()) {
            chatPanel.hide();
            chatToggleButton.setShowIcon();
        } else {
            chatPanel.show();
            chatToggleButton.setHideIcon();
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
