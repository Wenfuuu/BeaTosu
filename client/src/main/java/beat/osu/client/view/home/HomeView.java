package beat.osu.client.view.home;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import beat.osu.client.controller.BeatmapController;
import beat.osu.client.controller.ScoreController;
import beat.osu.client.enums.ScoreFilter;
import beat.osu.client.enums.SfxType;
import beat.osu.client.events.game.ReplayEvent;
import beat.osu.client.helper.AuthManager;
import beat.osu.client.helper.BackgroundManager;
import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ResourceManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.helper.ViewManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BeatmapSet;
import beat.osu.client.utils.OsuParser;
import beat.osu.client.utils.ReplayUtils;
import beat.osu.client.view.home.component.BeatmapContent;
import beat.osu.client.view.home.component.BottomBar;
import beat.osu.client.view.home.component.ScoreContent;
import beat.osu.client.view.game.component.overlays.ResultOverlay;
import beat.osu.client.view.home.component.TopBar;
import beat.osu.client.view.home.component.UploadBox;
import beat.osu.client.view.shared.common.Page;
import beat.osu.client.view.shared.common.Toast;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.responses.GetAllBeatmapsResponse;
import beat.osu.shared.dto.score.ScoreDto;
import beat.osu.shared.dto.score.responses.GetAllScoresResponse;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HomeView extends Page {

    private BeatmapController beatmapController;
    private ScoreController scoreController;

    private StackPane root;
    private BorderPane mainLayout;
    private TopBar topBar;
    private BottomBar bottomBar;
    private VBox leftBar;
    private VBox rightBar;
    private BeatmapContent beatmapContent;
    private UploadBox uploadBox;
    private ScoreContent scoreContent;
    private ResultOverlay scoreOverlay;
    private ArrayList<Beatmap> beatmaps;
    private ArrayList<ScoreDto> scores;

    private HBox searchArea;
    private Label searchLabel;
    private Label contentLabel;
    private Label foundLabel;
    private ComboBox<String> scoreFilterComboBox;

    private FadeTransition hideTransition;
    private FadeTransition showTransition;

    private String lastSearchQuery = "";
    private String persistedSearchQuery = "";
    private Timeline searchUpdateTimeline;

    public HomeView(Stage stage) {
        super(stage);
        setupView();
        handleEvent();
        setupCallbacks();
        setupAnimations();
        setupSearchUpdater();
        setupKeyHandlers();
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
        leftBar = new VBox();
        leftBar.setAlignment(Pos.TOP_LEFT);
        leftBar.setFillWidth(true);
        leftBar.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.3);
        leftBar.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.3);
        rightBar = new VBox();
        rightBar.setAlignment(Pos.TOP_RIGHT);
        rightBar.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.50);
        rightBar.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.50);

        createSearchArea();
        scoreFilterComboBox = new ComboBox<>();
        scoreFilterComboBox.getStyleClass().add("score-combo-box");
        scoreFilterComboBox.getItems().addAll(ScoreFilter.getAllScoreFilters());
        scoreFilterComboBox.getSelectionModel().selectFirst();
        scoreFilterComboBox.setOpacity(1.0);
        scoreFilterComboBox.prefWidthProperty().bind(leftBar.widthProperty());

        beatmapContent = new BeatmapContent(beatmaps);
        uploadBox = new UploadBox();
        uploadBox.setOnUploadCompleteCallback(this::refreshBeatmaps);
        uploadBox.setOnUploadProgressCallback(this::updateUploadStatus);

        uploadBox.setMaxWidth(Double.MAX_VALUE);
        uploadBox.setMinWidth(Region.USE_PREF_SIZE);

        scoreContent = new ScoreContent(new ArrayList<>());

        scores = new ArrayList<>();

        scoreOverlay = new ResultOverlay();

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
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        uploadBox.prefWidthProperty().bind(leftBar.widthProperty());
        uploadBox.setMaxWidth(Double.MAX_VALUE);
        uploadBox.setMinWidth(Region.USE_PREF_SIZE);

        scoreContent.prefWidthProperty().bind(leftBar.widthProperty());
        scoreContent.setMaxWidth(Double.MAX_VALUE);

        leftBar.getChildren().addAll(scoreFilterComboBox, scoreContent, spacer, uploadBox);
        rightBar.getChildren().addAll(searchArea, beatmapContent);

        VBox.setVgrow(beatmapContent, Priority.ALWAYS);

        mainLayout.setTop(topBar);
        mainLayout.setRight(rightBar);
        mainLayout.setBottom(bottomBar);
        mainLayout.setLeft(leftBar);

        root.getChildren().addAll(mainLayout, scoreOverlay);
    }

    @Override
    public void onShow() {
        setInputManager();
        if (inputManager != null) {
            inputManager.setSfxDisabled(false);
            inputManager.clearTypedChars();
            lastSearchQuery = "";
            
            if (!persistedSearchQuery.isEmpty()) {
                int matchesFound = beatmapContent.filterBeatmaps(persistedSearchQuery);
                inputManager.setTypedChars(persistedSearchQuery);
                contentLabel.setText(persistedSearchQuery);
                foundLabel.setText(String.format("%d matches found", matchesFound));
                foundLabel.setVisible(true);
                foundLabel.setManaged(true);
            } else {
                contentLabel.setText("Type to search!");
                foundLabel.setVisible(false);
                foundLabel.setManaged(false);
            }
        }
        if (searchUpdateTimeline != null) {
            searchUpdateTimeline.play();
        }

        beatmapContent.resetSelectionState();
        beatmapContent.triggerInitialSelection();
        scene.setRoot(root);
        
        // Request focus to ensure key events are received
        root.requestFocus();
        
        BgmManager.getInstance().playPreviewBgm(true);
        BackgroundManager.setGameBackground(scene);
    }

    private void createSearchArea() {
        searchArea = new HBox();
        searchArea.getStyleClass().add("search-area");
        searchArea.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.30);
        searchArea.setMaxWidth(ScreenManager.SCREEN_WIDTH * 0.30);
        searchArea.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.30);

        VBox searchContent = new VBox();

        HBox searchLine = new HBox();
        searchLabel = new Label("Search: ");
        searchLabel.getStyleClass().add("search-label");

        contentLabel = new Label("Type to search!");
        contentLabel.getStyleClass().add("search-content-label");

        searchLine.getChildren().addAll(searchLabel, contentLabel);

        foundLabel = new Label("");
        foundLabel.getStyleClass().add("search-found-label");
        foundLabel.setVisible(false);
        foundLabel.setManaged(false);

        searchContent.getChildren().addAll(searchLine, foundLabel);
        searchArea.getChildren().add(searchContent);
    }

    private void setupAnimations() {
        hideTransition = new FadeTransition(Duration.millis(500), mainLayout);
        hideTransition.setFromValue(1);
        hideTransition.setToValue(0);

        showTransition = new FadeTransition(Duration.millis(500), mainLayout);
        showTransition.setFromValue(0);
        showTransition.setToValue(1);
    }

    private void updateUploadStatus(String message) {
        if (message != null && !message.trim().isEmpty()) {
            if (message.contains("failed")) Toast.error(message).show();
            else Toast.success(message).show();
        }
    }

    private void setupSearchUpdater() {
        searchUpdateTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> updateSearch()));
        searchUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    private void setupKeyHandlers() {
        root.setOnKeyTyped(e -> {
            if (inputManager == null) return;
            String ch = e.getCharacter();
            if (!ch.isEmpty() && ch.charAt(0) >= 0x20) {
                inputManager.setTypedChars(ch);
                if (!inputManager.isSfxDisabled()) {
                    SfxManager.playMenuSfx(SfxType.KEY_PRESS);
                }
            }
            e.consume();
        });

        root.setOnKeyPressed(e -> {
            if (inputManager == null) return;
            if (e.getCode() == KeyCode.BACK_SPACE) {
                String current = inputManager.getTypedChars();
                if (!current.isEmpty()) {
                    inputManager.clearTypedChars();
                    inputManager.setTypedChars(current.substring(0, current.length() - 1));
                }
                if (!inputManager.isSfxDisabled()) {
                    SfxManager.playMenuSfx(SfxType.KEY_DELETE);
                }
            }
            e.consume();
        });
        
        // Make sure the root can receive key events
        root.setFocusTraversable(true);
    }

    private void updateSearch() {
        if (inputManager == null)
            return;

        String currentQuery = inputManager.getTypedChars().toLowerCase().trim();

        if (!currentQuery.equals(lastSearchQuery)) {
            lastSearchQuery = currentQuery;
            persistedSearchQuery = currentQuery;

            int matchesFound = beatmapContent.filterBeatmaps(currentQuery);
            if (currentQuery.isEmpty()) {
                contentLabel.setText("Type to search!");
                foundLabel.setVisible(false);
                foundLabel.setManaged(false);
            } else {
                contentLabel.setText(currentQuery);
                foundLabel.setText(String.format("%d matches found", matchesFound));
                foundLabel.setVisible(true);
                foundLabel.setManaged(true);
            }
        }
    }

    private ArrayList<ScoreDto> fetchScores(Beatmap beatmap) {
        try {
            Result<GetAllScoresResponse> result = scoreController.getScoresByBeatmapId(beatmap.getBeatmapId()).get();
            ArrayList<ScoreDto> scores = new ArrayList<>();

            if (result.isSuccess()) {
                ArrayList<ScoreDto> scoreDtos = result.getValue().getScores();
                if (scoreDtos != null && !scoreDtos.isEmpty()) {
                    scores = scoreDtos;

                    String selectedFilter = scoreFilterComboBox.getSelectionModel().getSelectedItem();
                    if (ScoreFilter.LOCAL.getScoreFilter().equals(selectedFilter) && AuthManager.isAuthenticated()) {
                        int currentUserId = AuthManager.getUser().getId();
                        ArrayList<ScoreDto> filteredScores = new ArrayList<>();
                        for (ScoreDto score : scores) {
                            if (score.getUserId() == currentUserId) {
                                filteredScores.add(score);
                            }
                        }
                        scores = filteredScores;
                        System.out.println(
                                "Filtered to " + scores.size() + " local scores for user ID: " + currentUserId);
                    } else {
                        System.out.println("Fetched " + scores.size() + " global scores for beatmap ID: "
                                + beatmap.getBeatmapId());
                    }
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
        File tempDir = ResourceManager.getBeatmapDirectory();
        Set<String> validBeatmapDirs = new HashSet<>();

        if (tempDir.exists() && tempDir.isDirectory()) {
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

                    if (!validBeatmapDirs.contains(expectedDirName)) {
                        return;
                    }

                    BeatmapSet beatmapSet = new BeatmapSet(
                            beatmapDto.getBeatmapSetDto().getId(),
                            beatmapDto.getBeatmapSetDto().getTitle(),
                            beatmapDto.getBeatmapSetDto().getArtist(),
                            beatmapDto.getBeatmapSetDto().getCreator(),
                            beatmapDto.getBeatmapSetDto().getLength(),
                            beatmapDto.getBeatmapSetDto().getBpm());

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
                            beatmapSet);

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
        onBeatmapSelected(beatmap, true);
    }

    private void onBeatmapSelected(Beatmap beatmap, boolean updateBackground) {
        try {
            OsuParser.parseBeatmap(beatmap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        topBar.updateSongInfo(beatmap);
        BgmManager.getInstance().playPreviewBgm(false);

        if (updateBackground) {
            BackgroundManager.setGameBackground(scene);
        }

        scores = fetchScores(beatmap);
        scoreContent.populateScores(scores);
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

    private void setupCallbacks() {
        beatmapContent.setOnBeatmapSelectedCallback(this::onBeatmapSelected);
        beatmapContent.setOnBeatmapSelectedWithBackgroundCallback(this::onBeatmapSelected);
        beatmapContent.setOnBeatmapPlayCallback(this::startGame);
        scoreContent.setOnScoreSelectedCallback(this::onScoreSelected);
    }

    private void handleEvent() {
        scoreFilterComboBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            SfxManager.playMenuSfx(SfxType.SELECT_EXPAND);
        });

        scoreFilterComboBox.setCellFactory(listView -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };

            cell.setOnMouseEntered(event -> {
                if (!cell.isEmpty()) {
                    SfxManager.playMenuSfx(SfxType.MENU_HOVER);
                }
            });

            return cell;
        });

        scoreFilterComboBox.setOnAction(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_HIT);
            Beatmap selectedBeatmap = beatmapContent.getSelectedBeatmap();
            if (selectedBeatmap != null) {
                scores = fetchScores(selectedBeatmap);
                scoreContent.populateScores(scores);
            }
        });

        bottomBar.getLogoView().setOnMouseEntered(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_HOVER);
            bottomBar.getOnHoverTransition().play();
        });

        bottomBar.getLogoView().setOnMouseExited(e -> {
            bottomBar.getOnExitTransition().play();
        });

        bottomBar.getLogoView().setOnMouseClicked(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_HIT);
            System.out.println("clicking play button");
            Beatmap selectedBeatmap = beatmapContent.getSelectedBeatmap();
            if (selectedBeatmap != null) {
                BgmManager.getInstance().stopBgm();
                if (searchUpdateTimeline != null) {
                    searchUpdateTimeline.stop();
                }
                ViewManager.getInstance().showGameView(selectedBeatmap, false);
            }
        });

        bottomBar.getBackButton().setOnMouseClicked(e -> {
            System.out.println("Back button clicked");
            if (searchUpdateTimeline != null) {
                searchUpdateTimeline.stop();
            }
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
                ArrayList<ReplayEvent> replayEvents = ReplayUtils.loadReplay(osrFileName);
                if (replayEvents.isEmpty()) {
                    Toast.error("Replay file is empty").show();
                    return;
                }
                scoreOverlay.setVisible(false);
                showTransition.play();
                if (searchUpdateTimeline != null) {
                    searchUpdateTimeline.stop();
                }
                ViewManager.getInstance().showReplayView(beatmap, score.getUserId(), replayEvents);
            } catch (IOException ex) {
                if (ex.getMessage().contains("Replay file not found")) {
                    Toast.error("Replay file not found").show();
                } else {
                    Toast.error("Error loading replay: " + ex.getMessage()).show();
                }
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

    private void refreshBeatmaps() {
        beatmaps = fetchBeatmaps();
        beatmapContent.clearContent();

        beatmapContent = new BeatmapContent(beatmaps);
        beatmapContent.setOnBeatmapSelectedCallback(this::onBeatmapSelected);
        beatmapContent.setOnBeatmapSelectedWithBackgroundCallback(this::onBeatmapSelected);
        beatmapContent.setOnBeatmapPlayCallback(this::startGame);

        leftBar.getChildren().clear();
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        leftBar.getChildren().addAll(scoreFilterComboBox, scoreContent, spacer, uploadBox);

        rightBar.getChildren().clear();
        rightBar.getChildren().addAll(searchArea, beatmapContent);

        beatmapContent.triggerInitialSelection();

        lastSearchQuery = "";
        if (inputManager != null) {
            inputManager.clearTypedChars();
        }
    }

    private void startGame(Beatmap beatmap) {
        if (beatmap != null) {
            SfxManager.playMenuSfx(SfxType.MENU_HIT);
            System.out.println("Starting game for beatmap: " + beatmap.getBeatmapSet().getTitle());
            BgmManager.getInstance().stopBgm();
            if (searchUpdateTimeline != null) {
                searchUpdateTimeline.stop();
            }
            ViewManager.getInstance().showGameView(beatmap, false);
        }
    }
}
