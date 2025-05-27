package beat.osu.client.view.home;

import beat.osu.client.controller.BeatmapController;
import beat.osu.client.helper.BackgroundManager;
import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.utils.OsuParser;
import beat.osu.client.view.Page;
import beat.osu.client.view.game.GameView;
import beat.osu.client.view.home.component.BeatmapPane;
import beat.osu.client.view.home.component.BottomBar;
import beat.osu.client.view.home.component.TopBar;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;

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
    }

    @Override
    public void init() {
        beatmapController = new BeatmapController();

        root = new StackPane();
        root.getStyleClass().add("root");
        mainLayout = new BorderPane();

        beatmaps = beatmapController.fetchBeatmaps();

        topBar = new TopBar();
        bottomBar = new BottomBar();
        beatmapPane = new BeatmapPane(beatmaps);

        if (!beatmaps.isEmpty()) {
            topBar.updateSongInfo(beatmaps.get(0));
        }

        scene = new Scene(root, ScreenManager.SCREEN_WIDTH, ScreenManager.SCREEN_HEIGHT);
        URL cssUrl = CssManager.getHomeCssURL("HomeView.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Css file not found!");
        }

        try {
            BackgroundManager.setRandomBackground(scene);
            BackgroundManager.setDarkBackground(scene, true);
        } catch (Exception e) {
            System.err.println("Error setting background for HomeView: " + e.getMessage());
            e.printStackTrace();
            root.setStyle("-fx-background-color: #121212;");
        }
    }

    @Override
    public void setLayout() {
        mainLayout.setTop(topBar);
        mainLayout.setRight(beatmapPane);
        mainLayout.setBottom(bottomBar);
        root.getChildren().add(mainLayout);
    }

    public void handleEvent() {
        beatmapPane.setOnBeatmapSelectedListener(beatmap -> {
            topBar.updateSongInfo(beatmap);
            OsuParser.extractAndParse(beatmap);
            BgmManager.playGameBgm();
        });

        bottomBar.getLogoView().setOnMouseClicked(e -> {
            System.out.println("clicking play button");
            Beatmap selectedBeatmap = beatmapPane.getSelectedBeatmap();
            if (selectedBeatmap != null) {
                new GameView(stage, selectedBeatmap);
            }
        });

        bottomBar.getBackButton().setOnMouseClicked(e -> {
            System.out.println("Back button clicked");
        });

        AnimationTimer inputChecker = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (inputManager.getPressedKeys().contains(KeyCode.Z)) {
                    System.out.println("Holding Z key");
                }
                if (inputManager.getPressedKeys().contains(KeyCode.X)) {
                    System.out.println("Holding X key");
                }
            }
        };
        inputChecker.start();
    }
}
