package beat.osu.client.view.landing.component.bancho;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

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

        this.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.35);

        Label titleLabel = new Label("Chat Panel");
        titleLabel.getStyleClass().add("chat-title");

        this.getChildren().addAll(titleLabel);
    }

    public void show() {
        this.setVisible(true);
        this.setTranslateY(this.getHeight());
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), this);
        slideIn.setFromY(this.getHeight());
        slideIn.setToY(0);
        slideIn.play();
    }

    public void hide() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), this);
        slideOut.setFromY(0);
        slideOut.setToY(this.getHeight());
        slideOut.setOnFinished(e -> this.setVisible(false));
        slideOut.play();
    }

    public boolean isShowing() {
        return this.isVisible();
    }
}