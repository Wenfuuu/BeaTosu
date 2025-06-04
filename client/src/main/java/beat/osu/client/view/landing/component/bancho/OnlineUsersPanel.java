package beat.osu.client.view.landing.component.bancho;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;

public class OnlineUsersPanel extends VBox {

    public OnlineUsersPanel() {
        super();
        this.getStyleClass().add("online-users-panel");
        this.setVisible(false);

        URL cssUrl = CssManager.getLandingCssURL("OnlineUsersPanel.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        this.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.65);

        Label titleLabel = new Label("osu!Bancho");
        titleLabel.getStyleClass().add("online-users-title");
        
        Label usersLabel = new Label("11,048 Users Connected");
        usersLabel.getStyleClass().add("online-users-title");
        
        Label contentLabel = new Label("Bancho content will go here...");
        contentLabel.getStyleClass().add("online-users-title");
        
        this.getChildren().addAll(titleLabel, usersLabel, contentLabel);
    }
    
    public void show() {
        this.setVisible(true);
        this.setOpacity(0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> this.setVisible(false));
        fadeOut.play();
    }
    
    public boolean isShowing() {
        return this.isVisible();
    }
}
