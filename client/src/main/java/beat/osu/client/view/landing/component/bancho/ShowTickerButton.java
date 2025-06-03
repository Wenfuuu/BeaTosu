package beat.osu.client.view.landing.component.bancho;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;

import java.net.URL;
import java.util.Objects;

public class ShowTickerButton extends Button {
    private ImageView showTickerOnIcon;
    private ImageView showTickerOffIcon;

    @Getter
    private boolean isTickerShown = false;

    public ShowTickerButton() {
        super();

        URL cssUrl = CssManager.getLandingCssURL("BanchoButton.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }

        this.getStyleClass().add("bancho-button");

        try {
            showTickerOnIcon = new ImageView(new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/buttons/bancho/show_ticker_on.png")).toExternalForm()));
            setupImageView(showTickerOnIcon);

            showTickerOffIcon = new ImageView(new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/buttons/bancho/show_ticker_off.png")).toExternalForm()));
            setupImageView(showTickerOffIcon);

            this.setGraphic(showTickerOffIcon);

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

    public void setShowTickerOnIcon() {
        this.isTickerShown = true;
        this.setGraphic(showTickerOnIcon);
    }

    public void setShowTickerOffIcon() {
        this.isTickerShown = false;
        this.setGraphic(showTickerOffIcon);
    }
}