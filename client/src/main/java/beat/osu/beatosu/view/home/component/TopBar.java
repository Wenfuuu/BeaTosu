package beat.osu.beatosu.view.home.component;

import beat.osu.beatosu.helper.CssManager;
import beat.osu.beatosu.model.Beatmap;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;

public class TopBar extends HBox {
    private Label titleLbl;
    private Label mapperLbl;
    private Label songDetailLbl;
    private Label songObjectLbl;
    private Label songDifficultyLbl;

    public TopBar() {
        this.getStyleClass().add("top-bar");

        initializeComponents();
        setupLayout();
        loadStyles();
    }

    private void initializeComponents() {
        titleLbl = new Label();
        mapperLbl = new Label();
        songDetailLbl = new Label();
        songObjectLbl = new Label();
        songDifficultyLbl = new Label();
    }

    private void setupLayout() {
        VBox songDetailBox = new VBox();
        songDetailBox.getChildren().addAll(titleLbl, mapperLbl, songDetailLbl, songObjectLbl, songDifficultyLbl);

        this.getChildren().addAll(songDetailBox);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getHomeCssURL("TopBar.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    public void updateSongInfo(Beatmap beatmap) {
        titleLbl.setText(beatmap.getBeatmapSet().getTitle());
        mapperLbl.setText("Mapped by " + beatmap.getBeatmapSet().getCreator());

        String detail = String.format("Length: %s BPM: %s",
                beatmap.getBeatmapSet().getLength(),
                beatmap.getBeatmapSet().getBpm());
        songDetailLbl.setText(detail);

        songObjectLbl.setText("Circles: 19 Sliders: 25 Spinners: 1");

        String diff = String.format("CS:%.2f AR:%.2f OD:%.2f HP:%.2f Stars:%.2f",
                beatmap.getCircleSize(),
                beatmap.getApproachRate(),
                beatmap.getOverallDifficulty(),
                beatmap.getHpDrainRate(),
                beatmap.getStarRating());
        songDifficultyLbl.setText(diff);
    }
}
