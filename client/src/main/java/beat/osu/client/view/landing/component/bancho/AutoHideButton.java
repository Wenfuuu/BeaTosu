package beat.osu.client.view.landing.component.bancho;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;

import java.net.URL;
import java.util.Objects;

public class AutoHideButton extends Button {
    private ImageView autoHideOnIcon;
    private ImageView autoHideOffIcon;

    @Getter
    private boolean isAutoHideEnabled = false;

    public AutoHideButton() {
        super();

        URL cssUrl = CssManager.getLandingCssURL("BanchoButton.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        this.getStyleClass().add("bancho-button");

        try {
            autoHideOnIcon = new ImageView(new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/buttons/bancho/auto_hide_on.png")).toExternalForm()));
            setupImageView(autoHideOnIcon);

            autoHideOffIcon = new ImageView(new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/buttons/bancho/auto_hide_off.png")).toExternalForm()));
            setupImageView(autoHideOffIcon);

            this.setGraphic(autoHideOffIcon);

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

    public void setAutoHideOnIcon() {
        this.isAutoHideEnabled = true;
        this.setGraphic(autoHideOnIcon);
    }

    public void setAutoHideOffIcon() {
        this.isAutoHideEnabled = false;
        this.setGraphic(autoHideOffIcon);
    }
}