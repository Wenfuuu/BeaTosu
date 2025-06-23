package beat.osu.client.view.home;

import beat.osu.client.controller.BeatmapController;
import beat.osu.client.controller.ScoreController;
import beat.osu.client.helper.*;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BeatmapSet;
import beat.osu.client.utils.OsuParser;
import beat.osu.client.utils.ReplayUtils;
import beat.osu.client.view.home.component.*;
import beat.osu.client.view.shared.common.Page;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.responses.GetAllBeatmapsResponse;
import beat.osu.shared.dto.score.ScoreDto;
import beat.osu.shared.dto.score.responses.GetAllScoresResponse;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class HomeView extends Page {

    private BeatmapController beatmapController;
    private ScoreController scoreController;

    private StackPane root;
    private BorderPane mainLayout;
    private TopBar topBar;
    private BottomBar bottomBar;
    private BeatmapContent beatmapContent;
    private ScoreContent scoreContent;
    private ScoreOverlay scoreOverlay;
    private ArrayList<Beatmap> beatmaps;
    private ArrayList<ScoreDto> scores;

    private FadeTransition hideTransition;
    private FadeTransition showTransition;

    public HomeView(Stage stage) {
        super(stage);
        setupView();
        handleEvent();
        setupAnimations();
    }

    @Override
    public void init() {
        beatmapController = new BeatmapController();
        scoreController = new ScoreController();

        root = new StackPane();
        root.getStyleClass().add("root");

        Pane backgroundOverlay = new Pane();
        backgroundOverlay.setStyle("-fx-background-color: rgba(18, 18, 18, 0.5);");
        backgroundOverlay.prefWidthProperty().bind(root.widthProperty());
        backgroundOverlay.prefHeightProperty().bind(root.heightProperty());
        root.getChildren().add(backgroundOverlay);

        mainLayout = new BorderPane();

        beatmaps = fetchBeatmaps();

        topBar = new TopBar();
        bottomBar = new BottomBar();
        beatmapContent = new BeatmapContent(beatmaps);
        scoreContent = new ScoreContent(new ArrayList<>());

        if (!beatmaps.isEmpty()) {
            topBar.updateSongInfo(beatmaps.get(0));
            scores = fetchScores(beatmaps.get(0));
            scoreContent = new ScoreContent(scores);
        }

        scoreOverlay = new ScoreOverlay();

        scene.setRoot(root);
        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            scene.getStylesheets().add(globalCssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }

        URL cssUrl = CssManager.getHomeCssURL("HomeView.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }
    }

    @Override
    public void setLayout() {
        mainLayout.setTop(topBar);
        mainLayout.setRight(beatmapContent);
        mainLayout.setBottom(bottomBar);
        mainLayout.setLeft(scoreContent);

        root.getChildren().addAll(mainLayout, scoreOverlay);
    }

    @Override
    public void onShow() {
        try {
            OsuParser.parseBeatmap(beatmapContent.getSelectedBeatmap());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene.setRoot(root);
        BgmManager.getInstance().playPreviewBgm(true);
        BackgroundManager.setGameBackground(scene);
    }

    private void setupAnimations() {
        hideTransition = new FadeTransition(Duration.millis(500), mainLayout);
        hideTransition.setFromValue(1);
        hideTransition.setToValue(0);

        showTransition = new FadeTransition(Duration.millis(500), mainLayout);
        showTransition.setFromValue(0);
        showTransition.setToValue(1);
    }

    private ArrayList<ScoreDto> fetchScores(Beatmap beatmap) {
        try {
            Result<GetAllScoresResponse> result = scoreController.getScoresByBeatmapId(beatmap.getBeatmapId()).get();
            ArrayList<ScoreDto> scores = new ArrayList<>();

            if (result.isSuccess()) {
                ArrayList<ScoreDto> scoreDtos = result.getValue().getScores();
                if (scoreDtos != null && !scoreDtos.isEmpty()) {
                    scores = scoreDtos;
                    System.out.println("Fetched " + scores.size() + " scores for beatmap ID: " + beatmap.getBeatmapId());
                } else {
                    System.out.println("No scores found for beatmap ID: " + beatmap.getBeatmapId());
                }
            } else {
                System.err.println("Failed to fetch scores: " + result.getError().getMessage());
            }

            return scores;
        } catch (Exception e) {
            System.err.println("Error fetching scores: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private ArrayList<Beatmap> fetchBeatmaps() {
        File tempDir = ResourceManager.getTempDirectory();
        Set<String> validBeatmapDirs = new HashSet<>();

        if(tempDir.exists() && tempDir.isDirectory()) {
            for (File file : Objects.requireNonNull(tempDir.listFiles())) {
                if (file.isDirectory()) {
                    validBeatmapDirs.add(file.getName());
                }
            }
        }

        try {
            Result<GetAllBeatmapsResponse> result = beatmapController.getAllBeatmaps().get();
            ArrayList<Beatmap> beatmaps = new ArrayList<>();

            if (result.isSuccess()) {
                result.getValue().getBeatmaps().forEach(beatmapDto -> {
                    String expectedDirName = String.format("%d",
                            beatmapDto.getBeatmapSetId());

                    System.out.println("Expected dir name: " + expectedDirName);

                    if (!validBeatmapDirs.contains(expectedDirName)) {
                        return;
                    }

                    BeatmapSet beatmapSet = new BeatmapSet(
                            beatmapDto.getBeatmapSetDto().getId(),
                            beatmapDto.getBeatmapSetDto().getTitle(),
                            beatmapDto.getBeatmapSetDto().getArtist(),
                            beatmapDto.getBeatmapSetDto().getCreator(),
                            beatmapDto.getBeatmapSetDto().getLength(),
                            beatmapDto.getBeatmapSetDto().getBpm()
                    );

                    Beatmap beatmap = new Beatmap(
                            beatmapDto.getId(),
                            beatmapDto.getBeatmapSetDto().getId(),
                            beatmapDto.getVersion(),
                            beatmapDto.getHpDrainRate(),
                            beatmapDto.getCircleSize(),
                            beatmapDto.getOverallDifficulty(),
                            beatmapDto.getApproachRate(),
                            beatmapDto.getSliderMultiplier(),
                            beatmapDto.getSliderTickRate(),
                            beatmapDto.getStarRating(),
                            beatmapSet
                    );

                    beatmaps.add(beatmap);
                });
            } else {
                System.err.println("Failed to fetch beatmaps: " + result.getError().getMessage());
            }

            return beatmaps;
        } catch (Exception e) {
            System.err.println("Error fetching beatmaps: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void onBeatmapSelected(Beatmap beatmap) {
        try {
            OsuParser.parseBeatmap(beatmap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        topBar.updateSongInfo(beatmap);
        BgmManager.getInstance().playPreviewBgm(false);
        BackgroundManager.setGameBackground(scene);

        scores = fetchScores(beatmap);
        scoreContent.populateScores(scores);
//         sfx testing purpose
//        for (String data: OsuParser.getHitObjects()) {
//            HitObjectFactory.createHitObject(data, beatmap, 1, 1);
//        }
    }

    private void onScoreSelected(ScoreDto score) {
        System.out.println("Score clicked: " + score.getId());
        scoreOverlay.updateResult(score, beatmapContent.getSelectedBeatmap());
        hideTransition.play();
        hideTransition.setOnFinished(e -> {
            scoreOverlay.setVisible(true);
            scoreOverlay.getShowTransition().play();
        });
    }

    private void handleEvent() {
        beatmapContent.setOnBeatmapSelectedCallback(this::onBeatmapSelected);
        scoreContent.setOnScoreSelectedCallback(this::onScoreSelected);

        bottomBar.getLogoView().setOnMouseClicked(e -> {
            System.out.println("clicking play button");
            Beatmap selectedBeatmap = beatmapContent.getSelectedBeatmap();
            if (selectedBeatmap != null) {
                BgmManager.getInstance().stopBgm();
//                new GameView(stage, selectedBeatmap);
                ViewManager.getInstance().showGameView(selectedBeatmap);
            }
        });

        bottomBar.getBackButton().setOnMouseClicked(e -> {
            System.out.println("Back button clicked");
            ViewManager.getInstance().showLandingView();
        });

        scoreOverlay.getReplayButton().setOnMouseClicked(e -> {
            ScoreDto score = scoreOverlay.getScore();
            Beatmap beatmap = beatmapContent.getSelectedBeatmap();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String formatted = score.getDate().format(formatter);
            String osrFileName = String.format("%s-%s-%s.osr",
                    score.getUserId(), beatmap.getBeatmapId(),
                    formatted.replace("/", "-").replace(":", "-"));

            try {
                scoreOverlay.getHideTransition().play();
                showTransition.play();
                ViewManager.getInstance().showReplayView(beatmap, ReplayUtils.loadReplay(osrFileName));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        scoreOverlay.getBackButton().setOnMouseClicked(e -> {
            scoreOverlay.getHideTransition().play();
            scoreOverlay.getHideTransition().setOnFinished(event -> {
                scoreOverlay.setVisible(false);
                showTransition.play();
            });
        });
    }
}
