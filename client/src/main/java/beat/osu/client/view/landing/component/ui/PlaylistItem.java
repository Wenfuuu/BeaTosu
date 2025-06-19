package beat.osu.client.view.landing.component.ui;

import java.io.File;
import java.net.URL;

import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import lombok.Getter;

public class PlaylistItem extends VBox {

    private Label songText;
    @Getter
    private String songPath;
    private boolean isSelected;
    @Getter
    private static PlaylistItem currentlySelected = null;

    public PlaylistItem(String songText, String songPath) {
        super();
        this.songText = new Label(songText);
        this.songPath = songPath;
        this.isSelected = false;

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
            setSelected(true);
            BgmManager.playBgm(new File(songPath));
        });
    }

    public void setSelected(boolean selected) {
        if (selected && currentlySelected != null && currentlySelected != this) {
            currentlySelected.setSelected(false);
        }
        
        this.isSelected = selected;
        
        if (selected) {
            this.getStyleClass().add("selected");
            currentlySelected = this;
        } else {
            this.getStyleClass().remove("selected");
            if (currentlySelected == this) {
                currentlySelected = null;
            }
        }
    }
}