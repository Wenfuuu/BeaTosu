package beat.osu.client.view.landing.component.bancho;

import beat.osu.client.helper.CssManager;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;

public class BanchoPanel extends VBox {

    public BanchoPanel() {
        super();
        this.getStyleClass().add("bancho-panel");
        this.setVisible(false);

        URL cssUrl = CssManager.getLandingCssURL("BanchoPanel.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        Label titleLabel = new Label("osu!Bancho");
        titleLabel.getStyleClass().add("bancho-title");
        
        Label usersLabel = new Label("11,048 Users Connected");
        usersLabel.getStyleClass().add("bancho-users");
        
        Label contentLabel = new Label("Bancho content will go here...");
        contentLabel.getStyleClass().add("bancho-content");
        
        this.getChildren().addAll(titleLabel, usersLabel, contentLabel);
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
