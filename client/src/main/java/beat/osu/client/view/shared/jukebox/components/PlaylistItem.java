package beat.osu.client.view.shared.jukebox.components;

import java.net.URL;
import java.util.function.Consumer;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.PlaylistManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.model.Song;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import lombok.Getter;
import lombok.Setter;

public class PlaylistItem extends VBox {

    @Getter
    private Song song;
    private Label songText;
    private boolean isSelected;
    @Setter
    private Consumer<PlaylistItem> onSelectionCallback;

    public PlaylistItem(Song song) {
        this.song = song;
        this.songText = new Label(song.getArtist() + " - " + song.getTitle());
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
            if (onSelectionCallback != null) {
                onSelectionCallback.accept(this);
            }
            PlaylistManager.getInstance().playSong(song);
        });
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        
        if (selected) {
            this.getStyleClass().add("selected");
        } else {
            this.getStyleClass().remove("selected");
        }
    }
}