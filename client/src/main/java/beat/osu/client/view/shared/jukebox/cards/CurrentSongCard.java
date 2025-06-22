package beat.osu.client.view.shared.jukebox.cards;

import java.net.URL;

import beat.osu.client.helper.CssManager;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

public class CurrentSongCard extends StackPane {

    @Getter
    private Label currentSongLabel;
    private HBox contentBox;
    private Rectangle underline;

    public CurrentSongCard(String text) {
        super();
        loadStyles();
        initializeComponents(text);
    }

    private void loadStyles() {
        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            this.getStylesheets().add(globalCssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        URL cssUrl = CssManager.getSharedCssURL("CurrentSongCard.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }
    }

    private void initializeComponents(String text) {
        this.currentSongLabel = new Label(text);
        this.contentBox = new HBox();
        this.underline = new Rectangle();

        contentBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        try {
            String imagePath = "/assets/images/song-background.png";
            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl != null) {
                Image backgroundImage = new Image(imageUrl.toExternalForm());
                BackgroundImage bgImage = new BackgroundImage(
                        backgroundImage,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                );
                contentBox.setBackground(new Background(bgImage));
            }
        } catch (Exception e) {
            System.err.println("Failed to load background image: " + e.getMessage());
        }

        contentBox.getStyleClass().add("current-song-box");
        this.currentSongLabel.getStyleClass().add("current-song-label");
        contentBox.getChildren().add(currentSongLabel);

        underline.setHeight(2.0);
        
        Stop[] stops = new Stop[] {
            new Stop(0, Color.TRANSPARENT),
            new Stop(0.8, Color.rgb(145, 145, 145, 0.5)),
            new Stop(1, Color.rgb(145, 145, 145))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE, stops);
        underline.setFill(gradient);
        
        underline.widthProperty().bind(this.widthProperty());
        
        StackPane.setAlignment(underline, javafx.geometry.Pos.BOTTOM_CENTER);
        this.getChildren().addAll(contentBox, underline);
    }
}
