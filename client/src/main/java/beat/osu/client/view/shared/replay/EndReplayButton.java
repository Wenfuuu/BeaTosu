package beat.osu.client.view.shared.replay;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;

import java.net.URL;
import java.util.Objects;

public class EndReplayButton extends Button {
    private ImageView currentIcon;

    @Getter
    private boolean isOnlineUserShown = false;

    public EndReplayButton() {
        super();

        URL cssUrl = CssManager.getSharedCssURL("BanchoButton.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        this.getStyleClass().add("bancho-button");

        try {
            currentIcon = new ImageView(new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/images/button/replay/end_replay.png")).toExternalForm()));
            setupImageView(currentIcon);

            this.setGraphic(currentIcon);

        } catch (Exception e) {
            System.err.println("Failed to load chat toggle icons: " + e.getMessage());
            this.setText("Chat");
        }
    }

    private void setupImageView(ImageView view) {
        view.setPreserveRatio(true);
        view.setFitHeight(27);
        view.setSmooth(true);
    }
}