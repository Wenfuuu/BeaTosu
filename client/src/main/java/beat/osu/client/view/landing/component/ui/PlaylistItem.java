package beat.osu.client.view.landing.component.ui;

import java.net.URL;

import beat.osu.client.helper.CssManager;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PlaylistItem extends VBox {

    private Label songText;

    public PlaylistItem(String songText) {
        super();
        loadStyles();

        this.getStyleClass().add("playlist-item");

        this.songText = new Label(songText);
        this.songText.getStyleClass().add("song-text");

//        this.setMinHeight(53.5);
//        this.setPrefHeight(53.5);
//        this.setMaxHeight(53.5);

        this.getChildren().add(this.songText);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getLandingCssURL("PlaylistItem.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }
}