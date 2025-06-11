package beat.osu.client.view.shared.bancho.buttons;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import lombok.Getter;

import java.net.URL;
import java.util.Objects;

public class ShowTickerButton extends Button {
    private ImageView showTickerOnIcon;
    private ImageView showTickerOffIcon;
    private ImageView currentIcon;

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

            currentIcon = showTickerOffIcon;
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

    private void animateIconChange(ImageView newIcon, Runnable onComplete) {
        StackPane container = new StackPane();
        
        currentIcon.setOpacity(1.0);
        container.getChildren().add(currentIcon);

        newIcon.setOpacity(0.0);
        container.getChildren().add(newIcon);
        
        this.setGraphic(container);
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(100), currentIcon);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(100), newIcon);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ParallelTransition parallelTransition = new ParallelTransition(fadeOut, fadeIn);
        parallelTransition.setOnFinished(e -> {
            this.setGraphic(newIcon);
            currentIcon = newIcon;
            onComplete.run();
        });
        parallelTransition.play();
    }

    public void setShowTickerOnIcon() {
        if (isTickerShown) return;

        ImageView newIcon = showTickerOnIcon;
        animateIconChange(newIcon, () -> isTickerShown = true);
    }

    public void setShowTickerOffIcon() {
        if (!isTickerShown) return;

        ImageView newIcon = showTickerOffIcon;
        animateIconChange(newIcon, () -> isTickerShown = false);
    }
}
