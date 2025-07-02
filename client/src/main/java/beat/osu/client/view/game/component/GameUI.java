package beat.osu.client.view.game.component;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.InputManager;
import beat.osu.client.helper.ScreenManager;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

@Getter
public class GameUI extends Pane {
    // private final Label comboLabel;
    private final ProgressBar healthBar;
    private Timeline healthAnimation;
    private final DoubleProperty animatedHealth = new SimpleDoubleProperty();

    // score
    private final ImageView[] scoreDigits;
    private final ImageView scoreComma;
    private final HBox scoreContainer;

    // accuracy
    private final ImageView[] accuracyDigits;
    private final ImageView percentSymbol;
    private final HBox accuracyContainer;

    // combo
    private final ArrayList<ImageView> comboDigits;
    private final ImageView comboXSymbol;
    private final HBox comboContainer; // input overlay
    private final ImageView[] inputOverlayImages;
    private final Text[] inputOverlayTexts;
    private final VBox inputOverlayContainer;

    private final Image[] digitImages;
    private final Image percentImage;
    private final Image xImage;
    private final Image commaImage;
    private final Image inputOverlayImage;
    private final Image inputOverlayBackgroundImage;
    private final SequentialTransition hideTransition;

    private boolean stillPerfect = true;

    public GameUI() {
        digitImages = new Image[10];
        for (int i = 0; i < 10; i++) {
            digitImages[i] = new Image(Objects.requireNonNull(Main.class
                    .getResource("/assets/images/score-" + i + ".png")).toExternalForm());
        }
        percentImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/score-percent.png")).toExternalForm());
        xImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/score-x.png")).toExternalForm());
        commaImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/score-comma.png")).toExternalForm());
        inputOverlayImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/inputoverlay-key.png")).toExternalForm());
        inputOverlayBackgroundImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/inputoverlay-background.png")).toExternalForm());

        // Score display
        scoreDigits = new ImageView[8];
        scoreContainer = new HBox(2); // 2px spacing between digits
        for (int i = 0; i < 8; i++) {
            scoreDigits[i] = new ImageView(digitImages[0]); // Start with all zeros
            scoreDigits[i].setFitWidth(40); // Adjust size as needed
            scoreDigits[i].setFitHeight(50);
            scoreDigits[i].setPreserveRatio(true);
            scoreContainer.getChildren().add(scoreDigits[i]);
        } // Input overlay
        inputOverlayImages = new ImageView[2];
        inputOverlayTexts = new Text[2];
        inputOverlayContainer = new VBox(0);

        // Get keybind letters from InputManager
        String[] keybindLetters = {
                InputManager.getKeybind1().getChar(),
                InputManager.getKeybind2().getChar()
        };

        for (int i = 0; i < 2; i++) {
            // Create key overlay
            inputOverlayImages[i] = new ImageView(inputOverlayImage);
            inputOverlayImages[i].setFitWidth(100);
            inputOverlayImages[i].setFitHeight(100);
            inputOverlayImages[i].setPreserveRatio(true);

            // Create keybind letter
            inputOverlayTexts[i] = new Text(keybindLetters[i]);
            inputOverlayTexts[i].setFont(Font.font("Aller", 24));
            inputOverlayTexts[i].setFill(Color.WHITE);

            // Create a simple container for each key (key image + letter)
            StackPane keyContainer = new StackPane();
            keyContainer.getChildren().addAll(inputOverlayImages[i], inputOverlayTexts[i]);

            inputOverlayContainer.getChildren().add(keyContainer);
        }

        // Combo display
        comboDigits = new ArrayList<>();
        comboXSymbol = new ImageView(xImage);
        comboContainer = new HBox(1);
        ImageView initialDigit = new ImageView(digitImages[0]);
        initialDigit.setFitWidth(45);
        initialDigit.setFitHeight(55);
        initialDigit.setPreserveRatio(true);
        comboDigits.add(initialDigit);
        comboXSymbol.setFitWidth(45);
        comboXSymbol.setFitHeight(55);
        comboXSymbol.setPreserveRatio(true);
        comboContainer.getChildren().addAll(initialDigit, comboXSymbol);

        // Accuracy display
        accuracyDigits = new ImageView[4];
        percentSymbol = new ImageView(percentImage);
        accuracyContainer = new HBox(1);
        scoreComma = new ImageView(commaImage);
        scoreComma.setFitWidth(10);
        scoreComma.setFitHeight(30);

        for (int i = 0; i < 4; i++) {
            if (i == 0)
                accuracyDigits[i] = new ImageView(digitImages[1]);
            else
                accuracyDigits[i] = new ImageView(digitImages[0]);

            accuracyDigits[i].setFitWidth(20);
            accuracyDigits[i].setFitHeight(30);
            accuracyDigits[i].setPreserveRatio(true);
            accuracyContainer.getChildren().add(accuracyDigits[i]);

            // Add decimal point for 100.0%
            if (i == 2) {
                accuracyContainer.getChildren().add(scoreComma);
            }
        }

        percentSymbol.setFitWidth(20);
        percentSymbol.setFitHeight(30);
        percentSymbol.setPreserveRatio(true);
        accuracyContainer.getChildren().add(percentSymbol);

        // Health bar
        healthBar = new ProgressBar(1.0);
        healthBar.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.5);
        healthBar.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.02);
        healthBar.progressProperty().bind(animatedHealth);
        animatedHealth.set(1.0);
        healthBar.getStyleClass().add("health-bar");

        VBox topLeftPanel = new VBox(5);
        topLeftPanel.getChildren().addAll(healthBar);
        topLeftPanel.setLayoutX(10);
        topLeftPanel.setLayoutY(10);

        // Top-right: Score and Accuracy
        VBox topRightPanel = new VBox(5);
        topRightPanel.getChildren().addAll(scoreContainer, accuracyContainer);
        topRightPanel.setAlignment(Pos.TOP_RIGHT);
        accuracyContainer.setAlignment(Pos.CENTER_RIGHT);

        // Bottom-left: Combo
        VBox bottomLeftPanel = new VBox(5);
        bottomLeftPanel.getChildren().add(comboContainer);

        StackPane inputOverlayPanel = new StackPane();
        inputOverlayPanel.getChildren().addAll(inputOverlayContainer);

        this.getChildren().addAll(topLeftPanel, topRightPanel, bottomLeftPanel, inputOverlayPanel);
        this.getProperties().put("topRightPanel", topRightPanel);
        this.getProperties().put("bottomLeftPanel", bottomLeftPanel);
        this.getProperties().put("inputOverlayPanel", inputOverlayPanel);

        FadeTransition fadeOutTransition = new FadeTransition(Duration.millis(500), this);
        fadeOutTransition.setFromValue(1);
        fadeOutTransition.setToValue(0);

        hideTransition = new SequentialTransition(
                fadeOutTransition,
                new PauseTransition(Duration.millis(500)));

        loadStyles();
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getGameCssURL("GameUI.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    public void updateSpectateHealth(double health) {
        if (healthBar.getProgress() != health) {
            if (healthAnimation != null) {
                healthAnimation.stop();
            }

            healthAnimation = new Timeline(
                    new KeyFrame(Duration.millis(10),
                            new KeyValue(animatedHealth, health, Interpolator.EASE_BOTH)));
            healthAnimation.play();
        }
    }

    public void updateHealth(double health) {
        if (healthBar.getProgress() != health) {
            if (healthAnimation != null) {
                healthAnimation.stop();
            }

            double currentHealth = healthBar.getProgress();
            double difference = Math.abs(currentHealth - health);
            Duration duration = Duration.millis(200 + (difference * 300));

            healthAnimation = new Timeline(
                    new KeyFrame(duration,
                            new KeyValue(animatedHealth, health, Interpolator.EASE_BOTH)));
            healthAnimation.play();
        }
    }

    public void updateScore(long score) {
        String scoreStr = String.format("%08d", Math.min(score, 99999999L)); // Max 8 digits

        for (int i = 0; i < 8; i++) {
            int digit = Character.getNumericValue(scoreStr.charAt(i));
            scoreDigits[i].setImage(digitImages[digit]);
        }
    }

    public void updateAccuracy(double accuracy) {
        if (accuracy == 100.0)
            return;
        int accuracyInt = (int) Math.round(accuracy * 100); // Convert to integer (9945 for 99.45%)

        String accuracyStr = String.format("%04d", Math.min(accuracyInt, 10000)); // Max 100.00%, pad to 4 digits

        if (stillPerfect) {
            accuracyContainer.getChildren().clear();
            for (int i = 0; i < 4; i++) {
                accuracyContainer.getChildren().add(accuracyDigits[i]);
                // Add decimal point after the second digit like 99.99%
                if (i == 1) {
                    accuracyContainer.getChildren().add(scoreComma);
                }
            }
            accuracyContainer.getChildren().add(percentSymbol);
            stillPerfect = false;
        }

        for (int i = 0; i < 4; i++) {
            int digit = Character.getNumericValue(accuracyStr.charAt(i));
            accuracyDigits[i].setImage(digitImages[digit]);
        }
    }

    public void updateCombo(int combo) {
        String comboStr = String.valueOf(combo);
        int requiredDigits = comboStr.length();

        comboContainer.getChildren().clear();
        comboDigits.clear();

        while (comboDigits.size() < requiredDigits) {
            ImageView newDigit = new ImageView(digitImages[0]);
            newDigit.setFitWidth(45);
            newDigit.setFitHeight(55);
            newDigit.setPreserveRatio(true);
            comboDigits.add(0, newDigit);
        }

        // Update digit images
        for (int i = 0; i < requiredDigits; i++) {
            int digit = Character.getNumericValue(comboStr.charAt(i));
            comboDigits.get(i).setImage(digitImages[digit]);
        }

        // Add all digits to container, then add x symbol
        comboContainer.getChildren().addAll(comboDigits);
        comboContainer.getChildren().add(comboXSymbol);
    }

    public void updateInputOverlay(boolean key1Pressed, boolean key2Pressed) {
        // Update the opacity or visual state of the key overlays based on key presses
        inputOverlayImages[0].setOpacity(key1Pressed ? 0.5 : 1.0);
        inputOverlayImages[1].setOpacity(key2Pressed ? 0.5 : 1.0);
    }
}
