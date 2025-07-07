package beat.osu.client.view.home.component;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import beat.osu.client.Main;
import beat.osu.client.factory.ButtonFactory;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.view.game.component.layout.ResultHeader;
import beat.osu.client.view.game.component.panels.HitCountsPanel;
import beat.osu.client.view.game.component.panels.ScorePanel;
import beat.osu.shared.dto.score.ScoreDto;
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

public class ScoreOverlay extends BorderPane {
    private final ImageView gradeSymbol;

    // Image resources
    private final Image[] digitImages;
    private Image gradeImage;

    private ResultHeader resultHeader;
    private ScorePanel scorePanel;
    private HitCountsPanel hitCountsPanel;

    private ImageView replayImageView;

    @Getter
    private Button replayButton;
    @Getter
    private Button backButton;

    @Getter
    private final FadeTransition showTransition;
    @Getter
    private final FadeTransition hideTransition;
    @Getter
    private ScoreDto score;

    public ScoreOverlay() {
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

        initializeComponents();
        setupLayout();
        loadStyles();

        showTransition = new FadeTransition(Duration.millis(500), this);
        showTransition.setFromValue(0);
        showTransition.setToValue(1);

        hideTransition = new FadeTransition(Duration.millis(500), this);
        hideTransition.setFromValue(1);
        hideTransition.setToValue(0);
    }

    public void updateResult(ScoreDto score, Beatmap beatmap) {
        this.score = score;
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");
        String formatted = now.format(formatter);

        String newSongTitle = String.format("%s - %s [%s]", beatmap.getBeatmapSet().getArtist(), beatmap.getBeatmapSet().getTitle(), beatmap.getVersion());
        String newCreator = beatmap.getBeatmapSet().getCreator();
        String newPlayedBy = String.format("Played by %s on %s.", score.getUsername(), formatted);

        resultHeader.updateLabels(newSongTitle, newCreator, newPlayedBy);

        scorePanel.updateScore(score.getScore());
        updateGrade(score.getGrade());

        hitCountsPanel.updateHitCounts(score.getPerfectHit(), score.getGekiHit(),
                score.getGreatHit(), score.getKatuHit(),
                score.getGoodHit(), score.getMiss());
        hitCountsPanel.updateCombo(score.getHighestCombo());
        hitCountsPanel.updateAccuracy(score.getAccuracy());
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

        Image replayImage = new Image(Objects.requireNonNull(Main.class.getResource("/assets/buttons/pause-menu/pause-replay.png")).toExternalForm());

        replayImageView = new ImageView(replayImage);

        replayButton = ButtonFactory.createResultReplayButton();
        backButton = ButtonFactory.createBackButton();
    }

    private void setupLayout() {
        this.getStyleClass().add("score-overlay");

        VBox rightStats = new VBox(20);
        rightStats.getStyleClass().add("right-stats");
        rightStats.setPadding(new Insets(ScreenManager.SCREEN_HEIGHT * 0.044, 0, ScreenManager.SCREEN_HEIGHT * 0.044, 0));

        rightStats.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.275);
        rightStats.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.275);
        rightStats.setMaxWidth(ScreenManager.SCREEN_WIDTH * 0.275);

        rightStats.setMinHeight(ScreenManager.SCREEN_HEIGHT * 0.82);
        rightStats.setPrefHeight(ScreenManager.SCREEN_HEIGHT * 0.82);
        rightStats.setMaxHeight(ScreenManager.SCREEN_HEIGHT * 0.82);

        gradeSymbol.setSmooth(true);

        VBox spacer = new VBox();

        replayImageView.setFitWidth(ScreenManager.SCREEN_WIDTH * 0.23);
        replayImageView.setPreserveRatio(true);
        replayImageView.setSmooth(true);
        replayButton.setPadding(Insets.EMPTY);
        replayButton.setGraphic(replayImageView);

        VBox.setVgrow(spacer, Priority.ALWAYS);
        rightStats.getChildren().addAll(gradeSymbol, spacer, replayButton);

        Pane contentPane = new Pane();
        contentPane.getChildren().addAll(scorePanel, hitCountsPanel, backButton, rightStats);

        scorePanel.setLayoutX(ScreenManager.SCREEN_WIDTH * 0.02);
        scorePanel.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.04);

        hitCountsPanel.setLayoutX(ScreenManager.SCREEN_WIDTH * 0.02);
        hitCountsPanel.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.15);

        backButton.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.83);

        rightStats.setLayoutX(ScreenManager.SCREEN_WIDTH * 0.64);
        rightStats.setLayoutY(ScreenManager.SCREEN_HEIGHT * 0.03);

        this.setTop(resultHeader);
        this.setCenter(contentPane);
    }

    private void loadStyles() {
        URL cssUrl = Main.class.getResource("/assets/css/home/ScoreOverlay.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }
}
