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
import beat.osu.client.helper.BackgroundManager;
import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.InputManager;
import beat.osu.client.helper.ResourceManager;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

public class SelectBeatmapModal extends StackPane {

    private BeatmapController beatmapController;
    
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

    private FadeTransition hideTransition;
    private FadeTransition showTransition;

    @Setter
    private InputManager inputManager;
    private String lastSearchQuery = "";
    private Timeline searchUpdateTimeline;

    @Getter
    private Beatmap selectedBeatmap;
    
    @Setter
    private Consumer<Beatmap> onBeatmapSelectedCallback;

    public SelectBeatmapModal() {
        initializeComponents();
        setupLayout();
        loadStyles();
        setupAnimations();
        setupSearchUpdater();
        handleEvent();
        
        this.setVisible(false);
    }

    private void initializeComponents() {
        beatmapController = new BeatmapController();
        
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

        beatmaps = fetchBeatmaps();

        topBar = new TopBar();
        bottomBar = new BottomBar();
        
        rightBar = new VBox();
        rightBar.setAlignment(Pos.TOP_RIGHT);

        createSearchArea();

        beatmapContent = new BeatmapContent(beatmaps);

        if (!beatmaps.isEmpty()) {
            topBar.updateSongInfo(beatmaps.get(0));
        }
    }

    private void createSearchArea() {
        searchArea = new HBox();
        searchArea.getStyleClass().add("search-area");
        searchArea.prefWidthProperty().bind(rightBar.widthProperty().multiply(0.5));
        searchArea.setMaxWidth(Region.USE_PREF_SIZE);

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
            System.err.println("Could not load SelectBeatmapModal CSS: " + e.getMessage());
        }
    }

    private void setupAnimations() {
        hideTransition = new FadeTransition(Duration.millis(500), mainLayout);
        hideTransition.setFromValue(1);
        hideTransition.setToValue(0);

        showTransition = new FadeTransition(Duration.millis(500), mainLayout);
        showTransition.setFromValue(0);
        showTransition.setToValue(1);
    }

    private void setupSearchUpdater() {
        searchUpdateTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> updateSearch()));
        searchUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void updateSearch() {
        if (inputManager == null)
            return;

        String currentQuery = inputManager.getTypedChars().toLowerCase().trim();

        if (!currentQuery.equals(lastSearchQuery)) {
            lastSearchQuery = currentQuery;

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
        beatmapContent.setOnBeatmapSelectedCallback(this::onBeatmapSelected);

        bottomBar.getLogoView().setOnMouseClicked(e -> {
            System.out.println("Select button clicked");
            Beatmap selectedBeatmap = beatmapContent.getSelectedBeatmap();
            if (selectedBeatmap != null && onBeatmapSelectedCallback != null) {
                onBeatmapSelectedCallback.accept(selectedBeatmap);
                hide();
            }
        });

        bottomBar.getBackButton().setOnMouseClicked(e -> {
            System.out.println("Cancel button clicked");
            hide();
        });
    }

    private void onBeatmapSelected(Beatmap beatmap) {
        try {
            beat.osu.client.utils.OsuParser.parseBeatmap(beatmap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        topBar.updateSongInfo(beatmap);
        BgmManager.getInstance().playPreviewBgm(false);
        
        BackgroundManager.setModalBeatmapBackground(backgroundLayer, beatmap);
        
        selectedBeatmap = beatmap;
    }

    private ArrayList<Beatmap> fetchBeatmaps() {
        File tempDir = ResourceManager.getTempDirectory();
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

    public void show() {
        beatmaps = fetchBeatmaps();
        
        rightBar.getChildren().remove(beatmapContent);
        beatmapContent = new BeatmapContent(beatmaps);
        beatmapContent.setOnBeatmapSelectedCallback(this::onBeatmapSelected);
        rightBar.getChildren().add(1, beatmapContent);
        
        if (inputManager != null) {
            inputManager.clearTypedChars();
            lastSearchQuery = "";
            contentLabel.setText("Type to search!");
            foundLabel.setVisible(false);
            foundLabel.setManaged(false);
        }
        
        if (!beatmaps.isEmpty()) {
            Beatmap firstBeatmap = beatmaps.get(0);
            topBar.updateSongInfo(firstBeatmap);
            
            try {
                OsuParser.parseBeatmap(firstBeatmap);
                BackgroundManager.setModalBeatmapBackground(backgroundLayer, firstBeatmap);
                selectedBeatmap = firstBeatmap;
            } catch (IOException e) {
                System.err.println("Error parsing first beatmap for background: " + e.getMessage());
            }
        }
        
        this.setVisible(true);
        
        if (searchUpdateTimeline != null) {
            searchUpdateTimeline.play();
        }

        BgmManager.getInstance().playPreviewBgm(true);

        showTransition.play();
    }

    public void hide() {
        if (searchUpdateTimeline != null) {
            searchUpdateTimeline.stop();
        }

        hideTransition.play();
        hideTransition.setOnFinished(e -> {
            this.setVisible(false);
        });
    }
}
