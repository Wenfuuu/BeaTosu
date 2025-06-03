package beat.osu.client.view.landing.component.bancho;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class BanchoPanel extends VBox {

    public BanchoPanel() {
        super();
        this.getStyleClass().add("bancho-panel");
        this.setVisible(false);

        this.setBackground(new Background(
                new BackgroundFill(
                        Color.rgb(0, 0, 0, 0.5),
                        null,
                        Insets.EMPTY
                )
        ));

        // Simple placeholder content for now
        Label titleLabel = new Label("osu!Bancho");
        titleLabel.getStyleClass().add("bancho-title");
        
        Label usersLabel = new Label("11,048 Users Connected");
        usersLabel.getStyleClass().add("bancho-users");
        
        // Placeholder content
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
