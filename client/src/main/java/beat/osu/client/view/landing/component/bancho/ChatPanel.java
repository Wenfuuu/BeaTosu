package beat.osu.client.view.landing.component.bancho;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;

public class ChatPanel extends VBox {

    public ChatPanel() {
        super();
        this.getStyleClass().add("chat-panel");
        this.setVisible(false);

        URL cssUrl = CssManager.getLandingCssURL("ChatPanel.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        this.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.4);

        Label titleLabel = new Label("Chat Panel");
        titleLabel.getStyleClass().add("chat-title");

        this.getChildren().addAll(titleLabel);
    }

    public void show() {
        this.setVisible(true);
    }

    public void hide() {
        this.setVisible(false);
    }

    public boolean isShowing() {
        return this.isVisible();
    }
}