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
//    private final Label scoreLabel;
    private final Label comboLabel;
    private final Label accuracyLabel;
    private final ProgressBar healthBar;
    private final Label hitResultLabel;

    // score
    private final ImageView[] scoreDigits;
    private final HBox scoreContainer;

    // accuracy


    private final Image[] digitImages;
    private final Image percentImage;
    private final SequentialTransition hideTransition;

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
            scoreDigits[i].setFitWidth(20); // Adjust size as needed
            scoreDigits[i].setFitHeight(28);
            scoreDigits[i].setPreserveRatio(true);
            scoreContainer.getChildren().add(scoreDigits[i]);
        }

        // Combo display
        comboLabel = new Label("Combo: 0x");
        comboLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        comboLabel.setTextFill(Color.YELLOW);

        // Accuracy display
        accuracyLabel = new Label("Accuracy: 100.00%");
        accuracyLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        accuracyLabel.setTextFill(Color.LIGHTGREEN);

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
        topRightPanel.getChildren().addAll(scoreContainer, accuracyLabel);
        topRightPanel.setAlignment(Pos.TOP_RIGHT);

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
}
