package beat.osu.client.view.landing.component.ui;

import java.io.File;
import java.net.URL;

import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class PlaylistItem extends VBox {

    private Label songText;
    private String songPath;

    public PlaylistItem(String songText, String songPath) {
        super();
        this.songText = new Label(songText);
        this.songPath = songPath;

        setupUI();
        loadStyles();
        setupEventHandlers();
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getLandingCssURL("PlaylistItem.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void setupUI() {
        this.getStyleClass().add("playlist-item");
        this.songText.setFont(new Font("Aller Light", ScreenManager.SCREEN_HEIGHT / 30));
        this.getChildren().add(this.songText);
    }

    private void setupEventHandlers() {
        this.setOnMouseClicked(e -> {
            BgmManager.playBgm(new File(songPath));
        });
    }
}