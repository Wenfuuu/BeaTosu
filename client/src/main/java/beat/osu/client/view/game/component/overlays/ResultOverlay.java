package beat.osu.client.view.game.component.overlays;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import lombok.Getter;

public class ResultOverlay extends BorderPane {
    private final ImageView gradeSymbol;

    // Image resources
    private final Image[] digitImages;
    private Image gradeImage;

    private ResultHeader resultHeader;
    private ScorePanel scorePanel;
    private HitCountsPanel hitCountsPanel;

    private ImageView retryImageView;
    private ImageView replayImageView;

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
                    .getResource("/assets/images/score/digits/score-" + i + ".png")).toExternalForm());
        }
        gradeImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/score/ranking/ranking-x.png")).toExternalForm());

        gradeSymbol = new ImageView(gradeImage);
        gradeSymbol.setFitHeight(ScreenManager.SCREEN_HEIGHT * 0.33);
        gradeSymbol.setPreserveRatio(true);

        initializeComponents();
        setupLayout();
        loadStyles();

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
        updateGrade(gameEndEvent.getGrade());

        hitCountsPanel.updateHitCounts(gameEndEvent.getPerfectHits(), gameEndEvent.getGekiHits(),
                gameEndEvent.getGreatHits(), gameEndEvent.getKatuHits(),
                gameEndEvent.getGoodHits(), gameEndEvent.getMisses());
        hitCountsPanel.updateCombo(gameEndEvent.getHighestCombo());
        hitCountsPanel.updateAccuracy(gameEndEvent.getAccuracy());
    }

    private void updateGrade(String grade) {
        String gradeImagePath;
        if(grade.equals("SS")) gradeImagePath = "/assets/images/score/ranking/ranking-x.png";
        else gradeImagePath = "/assets/images/ranking-" + grade.toLowerCase() + ".png";
        gradeImage = new Image(Objects.requireNonNull(Main.class.getResource(gradeImagePath)).toExternalForm());
        gradeSymbol.setImage(gradeImage);
    }

    private void initializeComponents() {
        resultHeader = new ResultHeader();
        scorePanel = new ScorePanel(digitImages);
        hitCountsPanel = new HitCountsPanel(digitImages);

        Image retryImage = new Image(Objects.requireNonNull(Main.class.getResource("/assets/images/button/pause-menu/pause-retry.png")).toExternalForm());
        Image replayImage = new Image(Objects.requireNonNull(Main.class.getResource("/assets/images/button/pause-menu/pause-replay.png")).toExternalForm());

        retryImageView = new ImageView(retryImage);
        replayImageView = new ImageView(replayImage);

        retryButton = ButtonFactory.createResultRetryButton();
        replayButton = ButtonFactory.createResultReplayButton();
        backButton = ButtonFactory.createBackButton();
    }

    private void setupLayout() {
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

        retryImageView.setFitWidth(ScreenManager.SCREEN_WIDTH * 0.23);
        retryImageView.setPreserveRatio(true);
        retryImageView.setSmooth(true);
        retryButton.setPadding(Insets.EMPTY);
        retryButton.setGraphic(retryImageView);

        replayImageView.setFitWidth(ScreenManager.SCREEN_WIDTH * 0.23);
        replayImageView.setPreserveRatio(true);
        replayImageView.setSmooth(true);
        replayButton.setPadding(Insets.EMPTY);
        replayButton.setGraphic(replayImageView);

        VBox.setVgrow(spacer, Priority.ALWAYS);
        rightStats.getChildren().addAll(gradeSymbol, spacer, retryButton, replayButton);

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
        URL cssUrl = Main.class.getResource("/assets/css/game/ResultOverlay.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }
}