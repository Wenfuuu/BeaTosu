package beat.osu.client.view.game.component;

import beat.osu.client.Main;
import beat.osu.client.game.GameEndData;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.model.Beatmap;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ResultOverlay extends BorderPane {
    private Label scoreLabel;
    private Label songTitleLabel;
    private Label mapperLabel;
    private Label playedLabel;
    private Label comboLabel;
    private Label accuracyLabel;
    private VBox hitCountsBox;
    private Label gradeLabel;
    private Button retryButton;
    private Button backButton;
    private ImageView rankingView;

    @Getter
    private final FadeTransition showTransition;

    public ResultOverlay() {
        this.setVisible(false);

        initializeComponents();
        setupLayout();
        setupStyling();

        // show animation
        showTransition = new FadeTransition(Duration.millis(500), this);
        showTransition.setFromValue(0);
        showTransition.setToValue(1);
    }

    public void updateResult(GameEndData gameEndData, Beatmap beatmap) {
        String songTitle = String.format("%s - %s [%s]",
                beatmap.getBeatmapSet().getArtist(), beatmap.getBeatmapSet().getTitle(), beatmap.getVersion());
        songTitleLabel.setText(songTitle);
        mapperLabel.setText("Beatmap by " + beatmap.getBeatmapSet().getCreator());

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formatted = now.format(formatter);
        playedLabel.setText("Played by Guest on " + formatted + ".");

        scoreLabel.setText(String.valueOf(gameEndData.getScore()));
        comboLabel.setText(gameEndData.getHighestCombo() + "x");
        accuracyLabel.setText(String.format("%.2f%%", gameEndData.getAccuracy()));
        gradeLabel.setText(gameEndData.getGrade());

        setupHitCounts(gameEndData.getPerfectHits(), gameEndData.getGekiHits(),
                gameEndData.getGreatHits(), gameEndData.getKatuHits(),
                gameEndData.getGoodHits(), gameEndData.getMisses());
    }

    private void initializeComponents() {
        // Song info
        songTitleLabel = new Label("Aoi Eir - Lament [pkhg's Hard]");
        mapperLabel = new Label("Beatmap by bt24-2");
        playedLabel = new Label("Played by bt24-2 on 10/10/2013 04:30:28.");

        // Score display
        scoreLabel = new Label("03241090");

        // Stats
        comboLabel = new Label("313x");
        accuracyLabel = new Label("96.24%");

        // Hit counts container
        hitCountsBox = new VBox(ScreenManager.SCREEN_HEIGHT * 0.09);
//        setupHitCounts();

        // Rank
        gradeLabel = new Label("A");

        // Buttons
        retryButton = new Button("Retry");
        backButton = new Button("Back");

        // Ranking image
        Image rankingPanel = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/ranking-panel.png")).toExternalForm());
        rankingView = new ImageView(rankingPanel);
        rankingView.setFitWidth(ScreenManager.SCREEN_WIDTH * 0.45);
        rankingView.setFitHeight(ScreenManager.SCREEN_HEIGHT * 0.6);
        rankingView.setMouseTransparent(true);
    }

    private void setupHitCounts(int perfectHits, int gekiHits, int greatHits,
                                int katuHits, int goodHits, int misses) {
        hitCountsBox.getChildren().clear();

        HBox row1 = createHitCountRow("300", perfectHits + "x", "激", gekiHits + "x");
        HBox row2 = createHitCountRow("100", greatHits + "x", "喝", katuHits + "x");
        HBox row3 = createHitCountRow("50", goodHits + "x", "×", misses + "x");

        hitCountsBox.getChildren().addAll(row1, row2, row3);
    }

    private HBox createHitCountRow(String label1, String count1, String label2, String count2) {
        HBox row = new HBox(200);
        row.setAlignment(Pos.CENTER_LEFT);

        // Left side
        Label leftLabel = new Label(label1);
        Label leftCount = new Label(count1);
        leftLabel.setTextFill(Color.WHITE);
        leftCount.setTextFill(Color.WHITE);
        leftCount.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Right side
        Label rightLabel = new Label(label2);
        Label rightCount = new Label(count2);
        rightLabel.setTextFill(Color.WHITE);
        rightCount.setTextFill(Color.WHITE);
        rightCount.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        HBox leftBox = new HBox(10);
        leftBox.getChildren().addAll(leftLabel, leftCount);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        HBox rightBox = new HBox(10);
        rightBox.getChildren().addAll(rightLabel, rightCount);
        rightBox.setAlignment(Pos.CENTER_LEFT);

        row.getChildren().addAll(leftBox, rightBox);
        return row;
    }

    private void setupLayout() {
        // Header section
        VBox headerSection = new VBox(10);
        headerSection.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        headerSection.getChildren().addAll(songTitleLabel, mapperLabel, playedLabel);

        // Hit counts panel
        StackPane hitCountsPanel = new StackPane();
        hitCountsPanel.getChildren().add(hitCountsBox);

        // Combo and accuracy
        HBox comboAccuracyBox = new HBox(200);
        comboAccuracyBox.setAlignment(Pos.CENTER_LEFT);

        comboAccuracyBox.getChildren().addAll(
                new VBox(10, comboLabel),
                new VBox(10, accuracyLabel)
        );

        // Right side - Rank
        VBox rightStats = new VBox(20);
        rightStats.setAlignment(Pos.CENTER);

        Label rankingTitle = new Label("Ranking");
        rankingTitle.setTextFill(Color.WHITE);
        rankingTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        rightStats.getChildren().addAll(rankingTitle, gradeLabel, retryButton);

        Pane contentPane = new Pane();
        contentPane.getChildren().addAll(rankingView, scoreLabel,
                hitCountsPanel, comboAccuracyBox, backButton, rightStats);

        // Position ranking image as in original (towards the right)
        double rankingImageX = 0; // Keep original positioning
        rankingView.setLayoutX(rankingImageX);
        rankingView.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.1);

        // Position score label to align with the left edge of ranking image
        scoreLabel.setLayoutX(rankingImageX + 50); // Align with ranking image left edge
        scoreLabel.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.1);

        // Position hit counts panel to align with ranking image left edge
        hitCountsPanel.setLayoutX(rankingImageX + 50);
        hitCountsPanel.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.275);

        // Position combo/accuracy box to align with ranking image left edge
        comboAccuracyBox.setLayoutX(rankingImageX + 50);
        comboAccuracyBox.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.6);

        // Keep back button at bottom left
        backButton.setLayoutX(20);
        backButton.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.9);

        // Position right stats towards the right side as in original
        rightStats.setLayoutX(ScreenManager.SCREEN_WIDTH - 200);
        rightStats.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.2);

        this.setTop(headerSection);
        this.setCenter(contentPane);
    }

    private void setupStyling() {
        // Background
        this.setStyle("-fx-background-color: rgba(123, 123, 123, 0.8);");

        // Song title
        songTitleLabel.setTextFill(Color.WHITE);
        songTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        mapperLabel.setTextFill(Color.WHITE);
        mapperLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Player name
        playedLabel.setTextFill(Color.WHITE);
        playedLabel.setFont(Font.font("Arial", 14));

        // Score
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));

        // Combo and accuracy
        comboLabel.setTextFill(Color.WHITE);
        comboLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        accuracyLabel.setTextFill(Color.WHITE);
        accuracyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        // Rank
        gradeLabel.setTextFill(Color.LIME);
        gradeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        DropShadow rankShadow = new DropShadow();
        rankShadow.setColor(Color.BLACK);
        rankShadow.setOffsetX(3);
        rankShadow.setOffsetY(3);
        rankShadow.setRadius(5);
        gradeLabel.setEffect(rankShadow);

        // Buttons
        setupButton(retryButton, Color.ORANGE);
        setupButton(backButton, Color.LIGHTBLUE);
    }

    private void setupButton(Button button, Color color) {
        button.setPrefSize(120, 40);
        button.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14; " +
                        "-fx-background-radius: 5;",
                toHexString(color)
        ));

        // Hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle(button.getStyle() + "-fx-opacity: 0.8;");
        });

        button.setOnMouseExited(e -> {
            button.setStyle(button.getStyle().replace("-fx-opacity: 0.8;", ""));
        });
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}