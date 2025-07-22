package beat.osu.client.view.match.component.modals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import beat.osu.client.controller.BeatmapController;
import beat.osu.client.controller.MatchController;
import beat.osu.client.enums.SfxType;
import beat.osu.client.helper.*;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BeatmapSet;
import beat.osu.client.utils.OsuParser;
import beat.osu.client.view.home.component.BeatmapContent;
import beat.osu.client.view.home.component.BottomBar;
import beat.osu.client.view.home.component.TopBar;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.responses.GetAllBeatmapsResponse;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

public class SelectBeatmapModal extends StackPane {

    private BeatmapController beatmapController;
    @Setter
    private MatchController matchController;
    @Setter
    private int matchId;
    
    private Pane backgroundLayer;
    private Pane overlayLayer;
    private BorderPane mainLayout;
    private TopBar topBar;
    private BottomBar bottomBar;
    private VBox rightBar;
    private BeatmapContent beatmapContent;
    private ArrayList<Beatmap> beatmaps;

    private HBox searchArea;
    private Label searchLabel;
    private Label contentLabel;
    private Label foundLabel;

    private FadeTransition showTransition;

    @Setter
    private InputManager inputManager;
    private String lastSearchQuery = "";
    private String persistedSearchQuery = "";
    private Timeline searchUpdateTimeline;

    @Getter
    private Beatmap selectedBeatmap;
    
    @Setter
    private Beatmap currentMatchBeatmap;
    
    @Setter
    private Consumer<Beatmap> onBeatmapSelectedCallback;

    public SelectBeatmapModal(BeatmapController beatmapController) {
        this.beatmapController = beatmapController;
        initializeComponents();
        
        eagerLoadBeatmaps();
        
        setupLayout();
        loadStyles();
        setupAnimations();
        setupSearchUpdater();
        setupKeyHandlers();
        handleEvent();
        
        this.setVisible(false);
    }

    private void initializeComponents() {
        this.getStyleClass().add("root");

        backgroundLayer = new Pane();
        backgroundLayer.setStyle("-fx-background-color: rgba(18, 18, 18, 1.0);");
        backgroundLayer.prefWidthProperty().bind(this.widthProperty());
        backgroundLayer.prefHeightProperty().bind(this.heightProperty());
        this.getChildren().add(backgroundLayer);

        overlayLayer = new Pane();
        overlayLayer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);"); 
        overlayLayer.prefWidthProperty().bind(this.widthProperty());
        overlayLayer.prefHeightProperty().bind(this.heightProperty());
        overlayLayer.setMouseTransparent(true);
        this.getChildren().add(overlayLayer);

        mainLayout = new BorderPane();

        beatmaps = new ArrayList<>();

        topBar = new TopBar();
        bottomBar = new BottomBar();
        
        rightBar = new VBox();
        rightBar.setAlignment(Pos.TOP_RIGHT);
        rightBar.setMinWidth(ScreenManager.SCREEN_WIDTH * 0.60);
        rightBar.setPrefWidth(ScreenManager.SCREEN_WIDTH * 0.60);

        createSearchArea();

        beatmapContent = new BeatmapContent(beatmaps);
        beatmapContent.setOnBeatmapSelectedCallback(this::onBeatmapSelected);
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

    private void setupLayout() {
        rightBar.getChildren().addAll(searchArea, beatmapContent);
        VBox.setVgrow(beatmapContent, Priority.ALWAYS);

        mainLayout.setTop(topBar);
        mainLayout.setRight(rightBar);
        mainLayout.setBottom(bottomBar);

        this.getChildren().add(mainLayout);
    }

    private void loadStyles() {
        try {
            URL globalCssUrl = CssManager.getGlobalCssURL();
            if (globalCssUrl != null) {
                this.getStylesheets().add(globalCssUrl.toExternalForm());
            }

            URL cssUrl = CssManager.getHomeCssURL("HomeView.css");
            if (cssUrl != null) {
                this.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            // System.err.println("Could not load SelectBeatmapModal CSS: " + e.getMessage());
        }
    }

    private void setupAnimations() {
        showTransition = new FadeTransition(Duration.millis(500), mainLayout);
        showTransition.setFromValue(0);
        showTransition.setToValue(1);
    }

    private void setupSearchUpdater() {
        searchUpdateTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> updateSearch()));
        searchUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    private void setupKeyHandlers() {
        this.setOnKeyTyped(e -> {
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

        this.setOnKeyPressed(e -> {
            if (inputManager == null) return;
            if (e.getCode() == KeyCode.BACK_SPACE) {
                String current = inputManager.getTypedChars();
                if (!current.isEmpty()) {
                    inputManager.clearTypedChars();
                    inputManager.setTypedChars(current.substring(0, current.length() - 1));
                    if (!inputManager.isSfxDisabled()) {
                        SfxManager.playMenuSfx(SfxType.KEY_DELETE);
                    }
                }
            }
            e.consume();
        });
        
        this.setFocusTraversable(true);
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

    private void handleEvent() {
        bottomBar.getLogoView().setOnMouseEntered(e -> {
            SfxManager.playMenuSfx(SfxType.MENU_HOVER);
            bottomBar.getOnHoverTransition().play();
        });

        bottomBar.getLogoView().setOnMouseExited(e -> {
            bottomBar.getOnExitTransition().play();
        });

        bottomBar.getLogoView().setOnMouseClicked(e -> {
            if (selectedBeatmap != null && onBeatmapSelectedCallback != null) {
                onBeatmapSelectedCallback.accept(selectedBeatmap);
                hide();
            }
        });

        bottomBar.getBackButton().setOnMouseClicked(e -> {
            hide();
        });
    }

    private void onBeatmapSelected(Beatmap beatmap) {
        try {
            OsuParser.parseBeatmap(beatmap);
        } catch (IOException e) {
            // System.err.println("Error parsing beatmap: " + e.getMessage());
        }
        
        topBar.updateSongInfo(beatmap);
        BgmManager.getInstance().playPreviewBgm(false);
        BackgroundManager.setModalBeatmapBackground(backgroundLayer, beatmap);
        
        selectedBeatmap = beatmap;
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
            }

            return beatmaps;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void eagerLoadBeatmaps() {
        beatmaps = fetchBeatmaps();
        
        beatmapContent = new BeatmapContent(beatmaps);
        beatmapContent.setOnBeatmapSelectedCallback(this::onBeatmapSelected);
        
        if (!beatmaps.isEmpty()) {
            selectedBeatmap = beatmaps.get(0);
        }
    }

    private void updateCurrentSelection() {
        if (currentMatchBeatmap != null && beatmaps != null) {
            for (Beatmap beatmap : beatmaps) {
                if (beatmap.getBeatmapId() == currentMatchBeatmap.getBeatmapId()) {
                    selectedBeatmap = beatmap;
                    beatmapContent.setSelectedBeatmap(beatmap);
                    break;
                }
            }
        }
        
        if (selectedBeatmap == null && beatmapContent != null) {
            selectedBeatmap = beatmapContent.getSelectedBeatmap();
        }
        
        if (beatmapContent != null) {
            beatmapContent.resetSelectionState();
            beatmapContent.triggerInitialSelection();
            
            if (selectedBeatmap != null) {
                onBeatmapSelected(selectedBeatmap);
            }
        }
    }

    public void show() {
        if (matchController != null) {
            matchController.updateMatchChangingBeatmap(matchId, true).thenAccept(result -> {
                if (!result.isSuccess()) {
                    // System.err.println("Failed to set changing beatmap status to true: " + result.getError().getMessage());
                }
            });
        }
        
        updateCurrentSelection();

        if (inputManager != null) {
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
        
        this.setVisible(true);
        
        this.requestFocus();
        
        if (searchUpdateTimeline != null) {
            searchUpdateTimeline.play();
        }

        BgmManager.getInstance().playPreviewBgm(true);
        showTransition.play();
    }

    public void hide() {
        if (matchController != null) {
            matchController.updateMatchChangingBeatmap(matchId, false).thenAccept(result -> {
                if (!result.isSuccess()) {
                    // System.err.println("Failed to set changing beatmap status to false: " + result.getError().getMessage());
                }
            });
        }
        
        if (searchUpdateTimeline != null) {
            searchUpdateTimeline.stop();
        }
        
        clearBeatmapData();
        this.setVisible(false);
    }
    
    private void clearBeatmapData() {
        selectedBeatmap = null;
    }
}
