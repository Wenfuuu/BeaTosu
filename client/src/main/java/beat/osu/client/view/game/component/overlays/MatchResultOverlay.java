package beat.osu.client.view.game.component.overlays;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import beat.osu.client.Main;
import beat.osu.client.events.game.GameEndEvent;
import beat.osu.client.factory.ButtonFactory;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.view.game.component.layout.ResultHeader;
import beat.osu.client.view.game.component.panels.HitCountsPanel;
import beat.osu.client.view.game.component.panels.ScorePanel;
import beat.osu.client.view.game.component.ui.MatchResultContent;
import beat.osu.shared.dto.match.events.MatchScoreEvent;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

public class MatchResultOverlay extends BorderPane {
    private final ImageView gradeSymbol;

    // Image resources
    private final Image[] digitImages;
    private Image gradeImage;

    private ResultHeader resultHeader;
    private ScorePanel scorePanel;
    private HitCountsPanel hitCountsPanel;

    @Getter
    private Button backButton;

    @Getter
    private final FadeTransition showTransition;
    private final MatchResultContent matchResultContent;

    public MatchResultOverlay() {
        this.setVisible(false);

        digitImages = new Image[10];
        for (int i = 0; i < 10; i++) {
            digitImages[i] = new Image(Objects.requireNonNull(Main.class
                    .getResource("/assets/images/score/digits/score-" + i + ".png")).toExternalForm());
        }
        gradeImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/ranking-x.png")).toExternalForm());

        gradeSymbol = new ImageView(gradeImage);
        gradeSymbol.setFitHeight(ScreenManager.SCREEN_HEIGHT * 0.33);
        gradeSymbol.setPreserveRatio(true);

        matchResultContent = new MatchResultContent(new ArrayList<>());
        matchResultContent.setPrefWidth(300);
        matchResultContent.setMaxWidth(300);
        matchResultContent.setPrefHeight(400);
        matchResultContent.setMaxHeight(400);

        initializeComponents();
        setupLayout();
        loadStyles();

        showTransition = new FadeTransition(Duration.millis(500), this);
        showTransition.setFromValue(0);
        showTransition.setToValue(1);
    }

    public void updateResult(GameEndEvent gameEndEvent, Beatmap beatmap, ArrayList<MatchScoreEvent> matchScores) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");
        String formatted = now.format(formatter);

        String newSongTitle = String.format("%s - %s [%s]", beatmap.getBeatmapSet().getArtist(), beatmap.getBeatmapSet().getTitle(), beatmap.getVersion());
        String newCreator = beatmap.getBeatmapSet().getCreator();
        String newPlayedBy = String.format("Match played at %s.", formatted);

        resultHeader.updateLabels(newSongTitle, newCreator, newPlayedBy);

        scorePanel.updateScore(gameEndEvent.getScore());
        updateGrade(gameEndEvent.getGrade());

        hitCountsPanel.updateHitCounts(gameEndEvent.getPerfectHits(), gameEndEvent.getGekiHits(),
                gameEndEvent.getGreatHits(), gameEndEvent.getKatuHits(),
                gameEndEvent.getGoodHits(), gameEndEvent.getMisses());
        hitCountsPanel.updateCombo(gameEndEvent.getHighestCombo());
        hitCountsPanel.updateAccuracy(gameEndEvent.getAccuracy());

        matchResultContent.populateScores(matchScores);
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

        backButton = ButtonFactory.createBackButton();
    }

    private void setupLayout() {
        VBox middleStats = new VBox(20);
        middleStats.getStyleClass().add("middle-stats");
        middleStats.setPadding(new Insets(ScreenManager.SCREEN_HEIGHT * 0.044, 0, ScreenManager.SCREEN_HEIGHT * 0.044, 0));

        middleStats.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.275);
        middleStats.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.275);
        middleStats.setMaxWidth(ScreenManager.SCREEN_WIDTH * 0.275);

        middleStats.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.82);
        middleStats.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.82);
        middleStats.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.82);

        gradeSymbol.setSmooth(true);

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        middleStats.getChildren().addAll(gradeSymbol, spacer);

        // Right stats for MatchResultContent
        VBox rightStats = new VBox(20);
        rightStats.getStyleClass().add("right-stats");
        rightStats.setPadding(new Insets(ScreenManager.SCREEN_HEIGHT * 0.044, 0, ScreenManager.SCREEN_HEIGHT * 0.044, 0));

        rightStats.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.275);
        rightStats.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.275);
        rightStats.setMaxWidth(ScreenManager.SCREEN_WIDTH * 0.275);

        rightStats.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.82);
        rightStats.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.82);
        rightStats.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.82);

        rightStats.getChildren().add(matchResultContent);

        Pane contentPane = new Pane();
        contentPane.getChildren().addAll(scorePanel, hitCountsPanel, backButton, middleStats, rightStats);

        scorePanel.setLayoutX(ScreenManager.SCREEN_WIDTH * 0.02);
        scorePanel.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.04);

        hitCountsPanel.setLayoutX(ScreenManager.SCREEN_WIDTH * 0.02);
        hitCountsPanel.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.15);

        backButton.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.83);

        middleStats.setLayoutX(ScreenManager.SCREEN_WIDTH * 0.55);
        middleStats.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.03);

        rightStats.setLayoutX(ScreenManager.SCREEN_WIDTH * 0.82);
        rightStats.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.03);

        this.setTop(resultHeader);
        this.setCenter(contentPane);
    }

    private void loadStyles() {
        // Load CSS if needed - can be empty for now or add specific styling
    }
}