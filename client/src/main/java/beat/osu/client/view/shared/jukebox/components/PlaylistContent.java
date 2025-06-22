package beat.osu.client.view.shared.jukebox.components;

import java.net.URL;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import javafx.scene.control.ScrollPane;

public class PlaylistContent extends ScrollPane {
    
    public PlaylistContent() {
        loadStyles();

        this.getStyleClass().add("playlist-content");
        this.setFitToWidth(true);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        this.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.77);
        this.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.77);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getSharedCssURL("PlaylistContent.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }
}
