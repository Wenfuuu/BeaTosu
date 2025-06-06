package beat.osu.client.view.landing.component.bancho.panels;

import java.net.URL;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.view.landing.component.bancho.SelectChannelModal;
import beat.osu.client.view.landing.component.bancho.buttons.BanchoButtons;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ChatPanel extends VBox {

    private SelectChannelModal selectChannelModal;
    private OnlineUsersPanel onlineUsersPanel;
    private BanchoButtons banchoButtons;

    public ChatPanel(SelectChannelModal selectChannelModal, OnlineUsersPanel onlineUsersPanel, BanchoButtons banchoButtons) {
        super();
        this.selectChannelModal = selectChannelModal;
        this.onlineUsersPanel = onlineUsersPanel;
        this.banchoButtons = banchoButtons;

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

        Button testButton = new Button("Test Channel");
        testButton.setOnMouseClicked(e -> {
            selectChannelModal.show();
            this.hide();
            this.banchoButtons.setVisible(false);

            if (onlineUsersPanel.isShowing()) {
                onlineUsersPanel.hide();
            }
        });

        this.getChildren().addAll(titleLabel, testButton);
    }

    public void show() {
        this.setVisible(true);
        this.setTranslateY(this.getHeight());
        this.setOpacity(0);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), this);
        slideIn.setFromY(this.getHeight() / 4);
        slideIn.setToY(0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition showTransition = new ParallelTransition(slideIn, fadeIn);
        showTransition.play();
    }

    public void hide() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), this);
        slideOut.setFromY(0);
        slideOut.setToY(this.getHeight() / 4);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        ParallelTransition hideTransition = new ParallelTransition(slideOut, fadeOut);
        hideTransition.setOnFinished(e -> this.setVisible(false));
        hideTransition.play();
    }

    public boolean isShowing() {
        return this.isVisible();
    }
}