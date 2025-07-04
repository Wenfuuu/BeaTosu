package beat.osu.client.view.game.component;

import beat.osu.client.Main;
import beat.osu.client.events.game.GameEndEvent;
import beat.osu.client.factory.ButtonFactory;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.model.Beatmap;
import beat.osu.shared.dto.match.events.MatchScoreEvent;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MatchResultOverlay extends BorderPane {
    // Score display with images
    private final ImageView[] scoreDigits;
    private final HBox scoreContainer;

    // Combo display with images
    private final List<ImageView> comboDigits;
    private final ImageView comboXSymbol;
    private final HBox comboContainer;

    // Accuracy display with images
    private final ImageView[] accuracyDigits;
    private final ImageView percentSymbol;
    private final HBox accuracyContainer;
    private final ImageView scoreComma;
    private final ImageView gradeSymbol;

    // Hit count displays with images
    private final HBox[] hitCountRows;
    private final ImageView[] hitCountLabels;
    private final List<List<ImageView>> hitCountDigits;
    private final ImageView[] hitCountXSymbols;

    // Image resources
    private final Image[] digitImages;
    private final Image percentImage;
    private final Image xImage;
    private final Image commaImage;
    private Image gradeImage;
    private final Image[] hitImages;

    private Label songTitleLabel;
    private Label mapperLabel;
    private Label playedLabel;
    private VBox hitCountsBox;
    @Getter
    private Button backButton;
    private ImageView rankingView;

    @Getter
    private final FadeTransition showTransition;
    private final MatchResultContent matchResultContent;

    public MatchResultOverlay() {
        this.setVisible(false);

        // Load digit images
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
        gradeImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/ranking-x.png")).toExternalForm());

        // Load hit count images
        hitImages = new Image[6];
        hitImages[0] = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/hit300.png")).toExternalForm());
        hitImages[1] = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/hit300g.png")).toExternalForm());
        hitImages[2] = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/hit100.png")).toExternalForm());
        hitImages[3] = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/hit100k.png")).toExternalForm());
        hitImages[4] = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/hit50.png")).toExternalForm());
        hitImages[5] = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/hit0.png")).toExternalForm());

        // Initialize score display (8 digits)
        scoreDigits = new ImageView[8];
        scoreContainer = new HBox(3);
        for (int i = 0; i < 8; i++) {
            scoreDigits[i] = new ImageView(digitImages[0]);
            scoreDigits[i].setFitWidth(30);
            scoreDigits[i].setFitHeight(42);
            scoreDigits[i].setPreserveRatio(true);
            scoreContainer.getChildren().add(scoreDigits[i]);
        }

        // Initialize combo display
        comboDigits = new ArrayList<>();
        comboXSymbol = new ImageView(xImage);
        comboContainer = new HBox(2);

        // Start with "0x"
        ImageView initialComboDigit = new ImageView(digitImages[0]);
        initialComboDigit.setFitWidth(25);
        initialComboDigit.setFitHeight(35);
        initialComboDigit.setPreserveRatio(true);
        comboDigits.add(initialComboDigit);

        comboXSymbol.setFitWidth(25);
        comboXSymbol.setFitHeight(35);
        comboXSymbol.setPreserveRatio(true);

        comboContainer.getChildren().addAll(initialComboDigit, comboXSymbol);

        // Initialize accuracy display (4 digits + percent)
        accuracyDigits = new ImageView[4];
        percentSymbol = new ImageView(percentImage);
        accuracyContainer = new HBox(1);
        scoreComma = new ImageView(commaImage);
        scoreComma.setFitWidth(10);
        scoreComma.setFitHeight(30);

        for (int i = 0; i < 4; i++) {
            accuracyDigits[i] = new ImageView(digitImages[0]);
            accuracyDigits[i].setFitWidth(20);
            accuracyDigits[i].setFitHeight(28);
            accuracyDigits[i].setPreserveRatio(true);
            accuracyContainer.getChildren().add(accuracyDigits[i]);

            // Add decimal point after 2nd digit for xx.xx% format
            if (i == 1) {
                accuracyContainer.getChildren().add(scoreComma);
            }
        }

        percentSymbol.setFitWidth(20);
        percentSymbol.setFitHeight(28);
        percentSymbol.setPreserveRatio(true);
        accuracyContainer.getChildren().add(percentSymbol);

        // Initialize hit count displays (3 rows)
        hitCountRows = new HBox[3];
        hitCountLabels = new ImageView[6]; // 6 different hit types
        hitCountDigits = new ArrayList<>();
        hitCountXSymbols = new ImageView[6];

        for (int i = 0; i < 6; i++) {
            hitCountLabels[i] = new ImageView(hitImages[i]);
            hitCountLabels[i].setFitWidth(40);
            hitCountLabels[i].setFitHeight(40);
            hitCountLabels[i].setPreserveRatio(true);

            hitCountXSymbols[i] = new ImageView(xImage);
            hitCountXSymbols[i].setFitWidth(20);
            hitCountXSymbols[i].setFitHeight(28);
            hitCountXSymbols[i].setPreserveRatio(true);

            hitCountDigits.add(new ArrayList<>());
        }

        // Initialize 3 rows
        for (int i = 0; i < 3; i++) {
            hitCountRows[i] = new HBox(20);
            hitCountRows[i].setAlignment(Pos.CENTER_LEFT);
        }

        // grade
        gradeSymbol = new ImageView(gradeImage);
        gradeSymbol.setFitHeight(175);
        gradeSymbol.setPreserveRatio(true);

        matchResultContent = new MatchResultContent(new ArrayList<>());
        matchResultContent.setPrefWidth(300);
        matchResultContent.setMaxWidth(300);
        matchResultContent.setPrefHeight(400);
        matchResultContent.setMaxHeight(400);

        initializeComponents();
        setupLayout();
        setupStyling();

        // show animation
        showTransition = new FadeTransition(Duration.millis(500), this);
        showTransition.setFromValue(0);
        showTransition.setToValue(1);
    }

    public void updateResult(GameEndEvent gameEndEvent, Beatmap beatmap, ArrayList<MatchScoreEvent> matchScores) {
        String songTitle = String.format("%s - %s [%s]",
                beatmap.getBeatmapSet().getArtist(), beatmap.getBeatmapSet().getTitle(), beatmap.getVersion());
        songTitleLabel.setText(songTitle);
        mapperLabel.setText("Beatmap by " + beatmap.getBeatmapSet().getCreator());

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formatted = now.format(formatter);
        playedLabel.setText("Match played at " + formatted);

        updateScore(gameEndEvent.getScore());
        updateCombo(gameEndEvent.getHighestCombo());
        updateAccuracy(gameEndEvent.getAccuracy());
        updateGrade(gameEndEvent.getGrade());

        updateHitCounts(gameEndEvent.getPerfectHits(), gameEndEvent.getGekiHits(),
                gameEndEvent.getGreatHits(), gameEndEvent.getKatuHits(),
                gameEndEvent.getGoodHits(), gameEndEvent.getMisses());

        matchResultContent.populateScores(matchScores);
    }

    private void updateScore(long score) {
        String scoreStr = String.format("%08d", Math.min(score, 99999999L));
        for (int i = 0; i < 8; i++) {
            int digit = Character.getNumericValue(scoreStr.charAt(i));
            scoreDigits[i].setImage(digitImages[digit]);
        }
    }

    private void updateCombo(int combo) {
        String comboStr = String.valueOf(combo);
        int requiredDigits = comboStr.length();

        // Clear the container
        comboContainer.getChildren().clear();

        // Adjust the number of digit ImageViews
        while (comboDigits.size() < requiredDigits) {
            ImageView newDigit = new ImageView(digitImages[0]);
            newDigit.setFitWidth(25);
            newDigit.setFitHeight(35);
            newDigit.setPreserveRatio(true);
            comboDigits.add(0, newDigit);
        }

        while (comboDigits.size() > requiredDigits) {
            comboDigits.remove(0);
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

    private void updateAccuracy(double accuracy) {
        // Format accuracy to 2 decimal places (e.g., 96.24% -> "9624")
        int accuracyInt = (int) Math.round(accuracy * 100);
        String accuracyStr = String.format("%04d", Math.min(accuracyInt, 10000));

        for (int i = 0; i < 4; i++) {
            int digit = Character.getNumericValue(accuracyStr.charAt(i));
            accuracyDigits[i].setImage(digitImages[digit]);
        }
    }

    private void updateGrade(String grade) {
        String gradeImagePath;
        if(grade.equals("SS")) gradeImagePath = "/assets/images/ranking-x.png";
        else gradeImagePath = "/assets/images/ranking-" + grade.toLowerCase() + ".png";
        gradeImage = new Image(Objects.requireNonNull(Main.class.getResource(gradeImagePath)).toExternalForm());
        gradeSymbol.setImage(gradeImage);
    }

    private void updateHitCounts(int perfectHits, int gekiHits, int greatHits,
                                 int katuHits, int goodHits, int misses) {
        // Update hit counts: 300, 300g, 100, 100k, 50, miss
        int[] hitCounts = {perfectHits, gekiHits, greatHits, katuHits, goodHits, misses};

        for (int i = 0; i < 6; i++) {
            updateHitCountDigits(i, hitCounts[i]);
        }

        // Clear and rebuild rows
        for (int i = 0; i < 3; i++) {
            hitCountRows[i].getChildren().clear();
        }

        // Row 1: 300 and 300g (激)
        hitCountRows[0].getChildren().addAll(
                createHitCountDisplay(0), // 300
                createHitCountDisplay(1)  // 300g
        );

        // Row 2: 100 and 100k (喝)
        hitCountRows[1].getChildren().addAll(
                createHitCountDisplay(2), // 100
                createHitCountDisplay(3)  // 100k
        );

        // Row 3: 50 and miss
        hitCountRows[2].getChildren().addAll(
                createHitCountDisplay(4), // 50
                createHitCountDisplay(5)  // miss
        );
    }

    private void updateHitCountDigits(int hitType, int count) {
        String countStr = String.valueOf(count);
        int requiredDigits = countStr.length();

        List<ImageView> digits = hitCountDigits.get(hitType);

        // Adjust the number of digit ImageViews
        while (digits.size() < requiredDigits) {
            ImageView newDigit = new ImageView(digitImages[0]);
            newDigit.setFitWidth(20);
            newDigit.setFitHeight(28);
            newDigit.setPreserveRatio(true);
            digits.add(0, newDigit);
        }

        while (digits.size() > requiredDigits) {
            digits.remove(0);
        }

        // Update digit images
        for (int i = 0; i < requiredDigits; i++) {
            int digit = Character.getNumericValue(countStr.charAt(i));
            digits.get(i).setImage(digitImages[digit]);
        }
    }

    private HBox createHitCountDisplay(int hitType) {
        HBox display = new HBox(5);
        display.setAlignment(Pos.CENTER_LEFT);
        display.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.25);

        // Add hit type image
        display.getChildren().add(hitCountLabels[hitType]);

        // Add count digits
        display.getChildren().addAll(hitCountDigits.get(hitType));

        // Add x symbol
        display.getChildren().add(hitCountXSymbols[hitType]);

        return display;
    }

    private void initializeComponents() {
        // Song info
        songTitleLabel = new Label("Aoi Eir - Lament [pkhg's Hard]");
        mapperLabel = new Label("Beatmap by bt24-2");
        playedLabel = new Label("Played by bt24-2 on 10/10/2013 04:30:28.");

        // Hit counts container
        hitCountsBox = new VBox(ScreenManager.SCREEN_HEIGHT * 0.06);
        hitCountsBox.getChildren().addAll(hitCountRows);

        // Buttons
        backButton = ButtonFactory.createBackButton();

        // Ranking image
        Image rankingPanel = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/ranking-panel.png")).toExternalForm());
        rankingView = new ImageView(rankingPanel);
        rankingView.setFitWidth(ScreenManager.SCREEN_WIDTH * 0.45);
        rankingView.setFitHeight(ScreenManager.SCREEN_HEIGHT * 0.6);
        rankingView.setMouseTransparent(true);
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
        HBox comboAccuracyBox = new HBox(ScreenManager.SCREEN_WIDTH * 0.175);
        comboAccuracyBox.setAlignment(Pos.CENTER_LEFT);

        comboAccuracyBox.getChildren().addAll(
                new VBox(10, comboContainer),
                new VBox(10, accuracyContainer)
        );

        // Right side - Rank
        VBox rightStats = new VBox(20);
        rightStats.setAlignment(Pos.CENTER);

        rightStats.getChildren().addAll(gradeSymbol, matchResultContent);

        Pane contentPane = new Pane();
        contentPane.getChildren().addAll(rankingView, scoreContainer,
                hitCountsPanel, comboAccuracyBox, backButton, rightStats);

        // Position ranking image as in original (towards the right)
        double rankingImageX = 0; // Keep original positioning
        rankingView.setLayoutX(rankingImageX);
        rankingView.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.1);

        // Position score container to align with the left edge of ranking image
        scoreContainer.setLayoutX(rankingImageX + 50);
        scoreContainer.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.1);

        // Position hit counts panel to align with ranking image left edge
        hitCountsPanel.setLayoutX(rankingImageX + 50);
        hitCountsPanel.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.275);

        // Position combo/accuracy box to align with ranking image left edge
        comboAccuracyBox.setLayoutX(rankingImageX + 50);
        comboAccuracyBox.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.6);

        // Keep back button at bottom left
        backButton.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.85);

        // Position right stats towards the right side as in original
        rightStats.setLayoutX(ScreenManager.SCREEN_WIDTH * 0.80);
        rightStats.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.2);

        this.setTop(headerSection);
        this.setCenter(contentPane);
    }

    private void setupStyling() {
        // Background
        this.setStyle("-fx-background-color: rgba(123, 123, 123, 0.8);");

        // Song title
        songTitleLabel.setTextFill(Color.WHITE);
        songTitleLabel.setFont(Font.font("Aller", FontWeight.BOLD, 24));

        mapperLabel.setTextFill(Color.WHITE);
        mapperLabel.setFont(Font.font("Aller", FontWeight.BOLD, 24));

        // Player name
        playedLabel.setTextFill(Color.WHITE);
        playedLabel.setFont(Font.font("Aller", 14));
    }
}
