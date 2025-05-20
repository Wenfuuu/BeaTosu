package beat.osu.beatosu.view.home;

import beat.osu.beatosu.controller.BeatmapController;
import beat.osu.beatosu.helper.CssManager;
import beat.osu.beatosu.helper.ScreenManager;
import beat.osu.beatosu.model.Beatmap;
import beat.osu.beatosu.view.Page;
import beat.osu.beatosu.view.home.component.BeatmapPane;
import beat.osu.beatosu.view.home.component.BottomBar;
import beat.osu.beatosu.view.home.component.TopBar;
import javafx.scene.Scene;
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

        // Setup initial state
        if (!beatmaps.isEmpty()) {
            topBar.updateSongInfo(beatmaps.getFirst());
        }

        scene = new Scene(root, ScreenManager.SCREEN_WIDTH, ScreenManager.SCREEN_HEIGHT);
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
        root.getChildren().add(mainLayout);
    }

    public void handleEvent() {
        // Set up beatmap selection listener
        beatmapPane.setOnBeatmapSelectedListener(beatmap -> {
            topBar.updateSongInfo(beatmap);
        });

        // Set up play button listener
        bottomBar.getLogoView().setOnMouseClicked(e -> {
            System.out.println("clicking play button");
            Beatmap selectedBeatmap = beatmapPane.getSelectedBeatmap();
//            if (selectedBeatmap != null) {
//                new PlayPage(stage, selectedBeatmap);
//            }
        });

        // You can add back button listener here if needed
        bottomBar.getBackButton().setOnAction(e -> {
            System.out.println("Back button clicked");
            // Handle back button action
        });
    }
}
