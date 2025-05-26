package beat.osu.client.view.game.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.Data;

@Data
public class GameUI extends Pane {
    private Label scoreLabel;
    private Label comboLabel;
    private Label accuracyLabel;
    private ProgressBar healthBar;
    private Label gameStatusLabel;
    private Label hitResultLabel;

    public GameUI() {
        // Score display
        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        scoreLabel.setTextFill(Color.WHITE);

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

        // Game status
        gameStatusLabel = new Label("Playing");
        gameStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gameStatusLabel.setTextFill(Color.CYAN);

        VBox topLeftPanel = new VBox(5);
        topLeftPanel.getChildren().addAll(healthLabel, healthBar, gameStatusLabel);
        topLeftPanel.setLayoutX(10);
        topLeftPanel.setLayoutY(10);

        // Top-right: Score and Accuracy
        VBox topRightPanel = new VBox(5);
        topRightPanel.getChildren().addAll(scoreLabel, accuracyLabel);
        topRightPanel.setAlignment(Pos.TOP_RIGHT);

        // Bottom-left: Combo
        VBox bottomLeftPanel = new VBox(5);
        bottomLeftPanel.getChildren().add(comboLabel);

        // Use a Pane instead of VBox for absolute positioning
        this.getChildren().addAll(topLeftPanel, topRightPanel, bottomLeftPanel, hitResultLabel);
        // Store references for layout updates
        this.getProperties().put("topRightPanel", topRightPanel);
        this.getProperties().put("bottomLeftPanel", bottomLeftPanel);
    }
}
