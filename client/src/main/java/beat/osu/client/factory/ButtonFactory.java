package beat.osu.client.factory;

import beat.osu.client.Main;
import beat.osu.client.helper.BackgroundManager;
import beat.osu.client.helper.SfxManager;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;

public class ButtonFactory {
    private static Button createPauseButton() {
        Button button = new Button();
        button.setStyle("-fx-padding: 0; -fx-border-width: 0; -fx-background-color: transparent;");

        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-padding: 0; -fx-border-width: 0; -fx-background-color: rgba(255, 255, 255, 0.2);");
            SfxManager.playSfx("pause-hover.wav");
        });
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-padding: 0; -fx-border-width: 0; -fx-background-color: transparent;");
        });

        return button;
    }

    public static Button createContinueButton() {
        Button continueButton = createPauseButton();
        BackgroundManager.setPauseButtonBackground(continueButton, "pause-continue.png");
        return continueButton;
    }

    public static Button createResultRetryButton() {
        Button pauseButton = createPauseButton();
        BackgroundManager.setResultButtonBackground(pauseButton, "pause-retry.png");
        return pauseButton;
    }

    public static Button createResultReplayButton() {
        Button replayButton = createPauseButton();
        BackgroundManager.setResultButtonBackground(replayButton, "pause-replay.png");
        return replayButton;
    }

    public static Button createRetryButton() {
        Button retryButton = createPauseButton();
        BackgroundManager.setPauseButtonBackground(retryButton, "pause-retry.png");
        return retryButton;
    }

    public static Button createLeaveButton() {
        Button continueButton = createPauseButton();
        BackgroundManager.setPauseButtonBackground(continueButton, "pause-back.png");
        return continueButton;
    }

    public static Button createBackButton() {
        Button button = new Button();
        try {
            String normalImagePath = "/assets/buttons/shared/global_back.png";
            String hoveredImagePath = "/assets/buttons/shared/global_back_hovered.png";

            URL normalImageUrl = Main.class.getResource(normalImagePath);
            URL hoveredImageUrl = Main.class.getResource(hoveredImagePath);

            if (normalImageUrl == null) {
                System.err.println("Image not found: " + normalImagePath);
                button.setText("〈 back");
            } else {
                Image normalImage = new Image(normalImageUrl.toExternalForm());

                ImageView normalImageView = new ImageView(normalImage);
                normalImageView.setFitHeight(50);
                normalImageView.setPreserveRatio(true);

                button.setGraphic(normalImageView);
                button.getStyleClass().clear();
                button.getStyleClass().add("back-button-image");
                button.setStyle("-fx-padding: 0; -fx-border-width: 0; -fx-background-color: transparent; -fx-background-insets: 0; -fx-background-radius: 0; -fx-border-radius: 0; -fx-effect: null; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

                button.setMinSize(normalImageView.getFitWidth(), normalImageView.getFitHeight());
                button.setMaxSize(normalImageView.getFitWidth(), normalImageView.getFitHeight());
                button.setPrefSize(normalImageView.getFitWidth(), normalImageView.getFitHeight());

                if (hoveredImageUrl != null) {
                    Image hoveredImage = new Image(hoveredImageUrl.toExternalForm());
                    ImageView hoveredImageView = new ImageView(hoveredImage);
                    hoveredImageView.setFitHeight(50);
                    hoveredImageView.setPreserveRatio(true);

                    hoveredImageView.setVisible(false);

                    StackPane imageStack = new StackPane();
                    imageStack.setAlignment(Pos.CENTER_LEFT);
                    imageStack.getChildren().addAll(normalImageView, hoveredImageView);

                    button.setGraphic(imageStack);

                    // Use the larger dimensions to accommodate both images
                    double normalWidth = normalImageView.getBoundsInLocal().getWidth();
                    double normalHeight = normalImageView.getBoundsInLocal().getHeight();
                    double hoveredWidth = hoveredImageView.getBoundsInLocal().getWidth();
                    double hoveredHeight = hoveredImageView.getBoundsInLocal().getHeight();

                    double maxWidth = Math.max(normalWidth, hoveredWidth);
                    double maxHeight = Math.max(normalHeight, hoveredHeight);

                    button.setMinSize(maxWidth, maxHeight);
                    button.setMaxSize(maxWidth, maxHeight);
                    button.setPrefSize(maxWidth, maxHeight);

                    // Create scale transitions for smooth hover effect
                    ScaleTransition scaleUp = new ScaleTransition(Duration.millis(80), imageStack);
                    scaleUp.setToX(1.05);
                    scaleUp.setToY(1.05);
                    scaleUp.setInterpolator(Interpolator.EASE_OUT);

                    ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), imageStack);
                    scaleDown.setToX(1.0);
                    scaleDown.setToY(1.0);
                    scaleDown.setInterpolator(Interpolator.EASE_OUT);

                    button.setOnMouseEntered(e -> {
                        scaleDown.stop();
                        normalImageView.setVisible(false);
                        hoveredImageView.setVisible(true);
                        scaleUp.play();
                    });

                    button.setOnMouseExited(e -> {
                        scaleUp.stop();
                        normalImageView.setVisible(true);
                        hoveredImageView.setVisible(false);
                        scaleDown.play();
                    });
                } else {
                    System.err.println("Hovered image not found: " + hoveredImagePath);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading back button images: " + e.getMessage());
            button.setText("〈 back");
            button.getStyleClass().add("back-button");
        }
        return button;
    }
}
