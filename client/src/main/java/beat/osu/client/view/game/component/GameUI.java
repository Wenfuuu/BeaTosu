package beat.osu.client.view.game.component;

import beat.osu.client.Main;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import lombok.Getter;

import java.util.Objects;

@Getter
public class GameUI extends Pane {
    private final Label comboLabel;
    private final ProgressBar healthBar;
    private final Label hitResultLabel;

    // score
    private final ImageView[] scoreDigits;
    private final HBox scoreContainer;

    // accuracy
    private final ImageView[] accuracyDigits;
    private final ImageView percentSymbol;
    private final HBox accuracyContainer;

    private final Image[] digitImages;
    private final Image percentImage;
    private final Label decimalPoint;
    private final SequentialTransition hideTransition;

    private boolean stillPerfect = true;

    public GameUI() {
        // Score display
        digitImages = new Image[10];
        for (int i = 0; i < 10; i++) {
            digitImages[i] = new Image(Objects.requireNonNull(Main.class
                    .getResource("/assets/images/score-" + i + ".png")).toExternalForm());
        }
        percentImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/score-percent.png")).toExternalForm());

        // Initialize score digit ImageViews (8 digits)
        scoreDigits = new ImageView[8];
        scoreContainer = new HBox(2); // 2px spacing between digits

        for (int i = 0; i < 8; i++) {
            scoreDigits[i] = new ImageView(digitImages[0]); // Start with all zeros
            scoreDigits[i].setFitWidth(40); // Adjust size as needed
            scoreDigits[i].setFitHeight(50);
            scoreDigits[i].setPreserveRatio(true);
            scoreContainer.getChildren().add(scoreDigits[i]);
        }

        // Combo display
        comboLabel = new Label("Combo: 0x");
        comboLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        comboLabel.setTextFill(Color.YELLOW);

        // Accuracy display
        accuracyDigits = new ImageView[4];
        percentSymbol = new ImageView(percentImage);
        accuracyContainer = new HBox(1);
        decimalPoint = new Label(".");
        decimalPoint.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        decimalPoint.setTextFill(Color.WHITE);
        decimalPoint.setStyle("-fx-padding: 0 2 4 2;"); // Adjust positioning

        for (int i = 0; i < 4; i++) {
            if(i == 0) accuracyDigits[i] = new ImageView(digitImages[1]);
            else accuracyDigits[i] = new ImageView(digitImages[0]);

            accuracyDigits[i].setFitWidth(20);
            accuracyDigits[i].setFitHeight(30);
            accuracyDigits[i].setPreserveRatio(true);
            accuracyContainer.getChildren().add(accuracyDigits[i]);

            // Add decimal point for 100.0%
            if (i == 2) {
                accuracyContainer.getChildren().add(decimalPoint);
            }
        }

        percentSymbol.setFitWidth(20);
        percentSymbol.setFitHeight(30);
        percentSymbol.setPreserveRatio(true);
        accuracyContainer.getChildren().add(percentSymbol);

        // Hit result display (appears temporarily when hitting objects)
        hitResultLabel = new Label("Perfect!");
        hitResultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        hitResultLabel.setVisible(false);

        // Health bar
        healthBar = new ProgressBar(1.0);
        healthBar.setPrefWidth(200);
        healthBar.setPrefHeight(20);
        healthBar.setStyle("-fx-accent: #ff4444;");

        Label healthLabel = new Label("Health");
        healthLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        healthLabel.setTextFill(Color.WHITE);

        VBox topLeftPanel = new VBox(5);
        topLeftPanel.getChildren().addAll(healthLabel, healthBar);
        topLeftPanel.setLayoutX(10);
        topLeftPanel.setLayoutY(10);

        // Top-right: Score and Accuracy
        VBox topRightPanel = new VBox(5);
        topRightPanel.getChildren().addAll(scoreContainer, accuracyContainer);
        topRightPanel.setAlignment(Pos.TOP_RIGHT);
        accuracyContainer.setAlignment(Pos.CENTER_RIGHT);

        // Bottom-left: Combo
        VBox bottomLeftPanel = new VBox(5);
        bottomLeftPanel.getChildren().add(comboLabel);

        // Use a Pane instead of VBox for absolute positioning
        this.getChildren().addAll(topLeftPanel, topRightPanel, bottomLeftPanel, hitResultLabel);
        // Store references for layout updates
        this.getProperties().put("topRightPanel", topRightPanel);
        this.getProperties().put("bottomLeftPanel", bottomLeftPanel);

        FadeTransition fadeOutTransition = new FadeTransition(Duration.millis(500), this);
        fadeOutTransition.setFromValue(1);
        fadeOutTransition.setToValue(0);

        hideTransition = new SequentialTransition(
                fadeOutTransition,
                new PauseTransition(Duration.millis(500))
        );
    }

    public void updateScore(long score) {
        String scoreStr = String.format("%08d", Math.min(score, 99999999L)); // Max 8 digits

        for (int i = 0; i < 8; i++) {
            int digit = Character.getNumericValue(scoreStr.charAt(i));
            scoreDigits[i].setImage(digitImages[digit]);
        }
    }

    public void updateAccuracy(double accuracy) {
        if(accuracy == 100.0) return;
        int accuracyInt = (int) Math.round(accuracy * 100); // Convert to integer (9945 for 99.45%)

        String accuracyStr = String.format("%04d", Math.min(accuracyInt, 10000)); // Max 100.00%, pad to 4 digits

        if(stillPerfect) {
            accuracyContainer.getChildren().clear();
            for (int i = 0; i < 4; i++) {
                accuracyContainer.getChildren().add(accuracyDigits[i]);
                // Add decimal point after the second digit like 99.99%
                if (i == 1) {
                    accuracyContainer.getChildren().add(decimalPoint);
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
}
