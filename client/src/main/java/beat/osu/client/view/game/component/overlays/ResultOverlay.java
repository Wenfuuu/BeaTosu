package beat.osu.client.view.game.component.overlays;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import beat.osu.client.Main;
import beat.osu.client.events.game.GameEndEvent;
import beat.osu.client.factory.ButtonFactory;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.view.game.component.layout.ResultHeader;
import beat.osu.client.view.game.component.panels.HitCountsPanel;
import beat.osu.client.view.game.component.panels.ScorePanel;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

public class ResultOverlay extends BorderPane {
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

    // Image resources
    private final Image[] digitImages;
    private final Image percentImage;
    private final Image xImage;
    private final Image commaImage;
    private Image gradeImage;

    private ResultHeader resultHeader;
    private ScorePanel scorePanel;
    private HitCountsPanel hitCountsPanel;
    @Getter
    private Button retryButton;
    @Getter
    private Button replayButton;
    @Getter
    private Button backButton;

    @Getter
    private final FadeTransition showTransition;

    public ResultOverlay() {
        this.setVisible(false);

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

        // grade
        gradeSymbol = new ImageView(gradeImage);
        gradeSymbol.setFitHeight(175);
        gradeSymbol.setPreserveRatio(true);

        initializeComponents();
        setupLayout();

        // show animation
        showTransition = new FadeTransition(Duration.millis(500), this);
        showTransition.setFromValue(0);
        showTransition.setToValue(1);
    }

    public void updateResult(GameEndEvent gameEndEvent, Beatmap beatmap) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");
        String formatted = now.format(formatter);
        String userName = AuthManager.isAuthenticated() ? AuthManager.getUser().getUsername() : "Guest";

        String newSongTitle = String.format("%s - %s [%s]", beatmap.getBeatmapSet().getArtist(), beatmap.getBeatmapSet().getTitle(), beatmap.getVersion());
        String newCreator = beatmap.getBeatmapSet().getCreator();
        String newPlayedBy = String.format("Played by %s on %s.", userName, formatted);

        resultHeader.updateLabels(newSongTitle, newCreator, newPlayedBy);

        scorePanel.updateScore(gameEndEvent.getScore());
        updateCombo(gameEndEvent.getHighestCombo());
        updateAccuracy(gameEndEvent.getAccuracy());
        updateGrade(gameEndEvent.getGrade());

        hitCountsPanel.updateHitCounts(gameEndEvent.getPerfectHits(), gameEndEvent.getGekiHits(),
                gameEndEvent.getGreatHits(), gameEndEvent.getKatuHits(),
                gameEndEvent.getGoodHits(), gameEndEvent.getMisses());
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

    private void initializeComponents() {
        resultHeader = new ResultHeader();
        scorePanel = new ScorePanel(digitImages);
        hitCountsPanel = new HitCountsPanel(digitImages);

        // Buttons
        retryButton = ButtonFactory.createResultRetryButton();
        replayButton = ButtonFactory.createResultReplayButton();
        backButton = ButtonFactory.createBackButton();
    }

    private void setupLayout() {
        HBox comboAccuracyBox = new HBox(ScreenManager.SCREEN_WIDTH * 0.175);
        comboAccuracyBox.setAlignment(Pos.CENTER_LEFT);

        comboAccuracyBox.getChildren().addAll(
                new VBox(10, comboContainer),
                new VBox(10, accuracyContainer)
        );

        // Right side - Rank
        VBox rightStats = new VBox(20);
        rightStats.setAlignment(Pos.CENTER);

        rightStats.getChildren().addAll(gradeSymbol, retryButton, replayButton);

        Pane contentPane = new Pane();
        contentPane.getChildren().addAll(scorePanel, hitCountsPanel, comboAccuracyBox, backButton, rightStats);

        scorePanel.setLayoutX(ScreenManager.SCREEN_WIDTH * 0.01);
        scorePanel.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.04);

        hitCountsPanel.setLayoutX(ScreenManager.SCREEN_WIDTH * 0.01);
        hitCountsPanel.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.12);

        comboAccuracyBox.setLayoutX(ScreenManager.SCREEN_WIDTH * 0.01);
        comboAccuracyBox.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.6);

        backButton.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.85);

        rightStats.setLayoutX(ScreenManager.SCREEN_WIDTH * 0.80);
        rightStats.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.2);

        this.setTop(resultHeader);
        this.setCenter(contentPane);
    }
}