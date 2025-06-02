package beat.osu.client.view.home;

import beat.osu.client.controller.BeatmapController;
import beat.osu.client.helper.*;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.BeatmapSet;
import beat.osu.client.utils.OsuParser;
import beat.osu.client.view.Page;
import beat.osu.client.view.game.GameView;
import beat.osu.client.view.home.component.BeatmapPane;
import beat.osu.client.view.home.component.BottomBar;
import beat.osu.client.view.home.component.TopBar;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.beatmap.responses.GetAllBeatmapsResponse;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class HomeView extends Page {

    private BeatmapController beatmapController;

    private StackPane root;
    private BorderPane mainLayout;
    private TopBar topBar;
    private BottomBar bottomBar;
    private BeatmapPane beatmapPane;
    private ArrayList<Beatmap> beatmaps;

    public HomeView(Stage stage) {
        super(stage);
        handleEvent();

        BgmManager.playPreviewBgm();
        BackgroundManager.setGameBackground(scene);
    }

    @Override
    public void init() {
        beatmapController = new BeatmapController();

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
        beatmapPane = new BeatmapPane(beatmaps);

        if (!beatmaps.isEmpty()) {
            topBar.updateSongInfo(beatmaps.get(0));
        }

        scene.setRoot(root);
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
        mainLayout.setRight(beatmapPane);
        mainLayout.setBottom(bottomBar);

        root.getChildren().addAll(mainLayout);
    }

    private ArrayList<Beatmap> fetchBeatmaps() {
        File tempDir = new File("./src/main/resources/assets/temp");
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
                            beatmapDto.getSlideMultiplier(),
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

    public void handleEvent() {
        beatmapPane.setOnBeatmapSelectedListener(beatmap -> {
            topBar.updateSongInfo(beatmap);
//            OsuParser.extractAndParse(beatmap);
            try {
                OsuParser.parseBeatmap(beatmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BgmManager.playPreviewBgm();
            BackgroundManager.setGameBackground(scene);

            // sfx testing purpose
//            for (String data: OsuParser.getHitObjects()) {
//                HitObjectFactory.createHitObject(data, beatmap,
//                        1, 1);
//            }
        });

        bottomBar.getLogoView().setOnMouseClicked(e -> {
            System.out.println("clicking play button");
            Beatmap selectedBeatmap = beatmapPane.getSelectedBeatmap();
            if (selectedBeatmap != null) {
                BgmManager.stopBgm();
//                new GameView(stage, selectedBeatmap);
                ViewManager.showGameView(selectedBeatmap);
            }
        });

        bottomBar.getBackButton().setOnMouseClicked(e -> {
            System.out.println("Back button clicked");
        });
    }
}
