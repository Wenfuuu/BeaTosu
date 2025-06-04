package beat.osu.client.view.landing.component.bancho;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;

public class OnlineUsersPanel extends VBox {

    private ArrayList<UserCard> userCards;

    public OnlineUsersPanel() {
        super();
        this.getStyleClass().add("online-users-panel");
        this.setVisible(false);

        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            this.getStylesheets().add(globalCssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        URL cssUrl = CssManager.getLandingCssURL("OnlineUsersPanel.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        this.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.65);

        this.userCards = new ArrayList<>();
        this.userCards.add(new UserCard(1, "Test", "ID", null, 1067, 98.12, 3013, 90));

        Label titleLabel = new Label("osu!Bancho");
        titleLabel.getStyleClass().add("online-users-title");
        
        Label onlineUsersLabel = new Label("11,048 Users Connected");
        onlineUsersLabel.getStyleClass().add("online-users-label");

        this.getChildren().addAll(titleLabel, onlineUsersLabel, userCards.get(0));
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
