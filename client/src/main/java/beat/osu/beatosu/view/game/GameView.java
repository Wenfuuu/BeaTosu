package beat.osu.beatosu.view.game;

import beat.osu.beatosu.helper.ScreenManager;
import beat.osu.beatosu.model.Beatmap;
import beat.osu.beatosu.utils.OsuParser;
import beat.osu.beatosu.utils.OszExtractor;
import beat.osu.beatosu.view.Page;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class GameView extends Page {
    // Osu! playfield resolution (4:3)
    private final double OSU_WIDTH = 512.0;
    private final double OSU_HEIGHT = 384.0;
    private final double OSU_ASPECT_RATIO = OSU_WIDTH / OSU_HEIGHT;

    private double circleSize; // Default Circle Size (CS) if parsing fails
    private double osuPixelDiameter;   // Diameter in original osu! coordinates

    // Game Loop Timer
    private AnimationTimer gameLoop;
    private long startTimeNanos = -1;

    private Pane root;

    public GameView(Stage stage, Beatmap selectedBeatmap) {
        super(stage);
        processBeatmap(selectedBeatmap);


    }

    private void addHitObject(String data){
//        root.getChildren().add(HitObjectFactory.createHitObject(data).getNode());
    }

    private void processBeatmap(Beatmap selectedBeatmap) {
        //search & extract .osz -> stored in temp folder
        String oszPath = String.format("./src/main/java/resources/assets/beatmap/%d %s - %s.osz", selectedBeatmap.getBeatmapSet().getBeatmapSetId(),
                selectedBeatmap.getBeatmapSet().getArtist(), selectedBeatmap.getBeatmapSet().getTitle());
        File oszFile = new File(oszPath);
        File outputDir = new File("./src/main/java/resources/assets/temp");
        try {
            OszExtractor.extractOsz(oszFile, outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //parse the selected .osu file
        String osuPath = String.format("./src/main/java/resources/assets/temp/%s - %s (%s) [%s].osu",
                selectedBeatmap.getBeatmapSet().getArtist(),
                selectedBeatmap.getBeatmapSet().getTitle(),
                selectedBeatmap.getBeatmapSet().getCreator(),
                selectedBeatmap.getVersion());
        File osuFile = new File(osuPath);
        try {
            OsuParser.parse(osuFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> diff = OsuParser.getDifficulty();
        circleSize = Double.parseDouble(diff.get("CircleSize"));

        for(String data: OsuParser.getHitObjects()) {
            addHitObject(data);
        }
    }

    @Override
    public void init() {
        root = new Pane();
        root.setStyle("-fx-background-color: #2C2C2C;");

        scene = new Scene(root, ScreenManager.SCREEN_WIDTH, ScreenManager.SCREEN_HEIGHT);
    }

    @Override
    public void setLayout() {

    }
}
