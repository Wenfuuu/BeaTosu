package beat.osu.client.view.home.component;

import beat.osu.client.helper.BackgroundManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.utils.OsuParser;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.ArrayList;
import java.util.function.Consumer;

public class BeatmapPane extends ScrollPane {
    private VBox beatmapListBox;
    private ArrayList<Beatmap> beatmaps;
    @Getter
    private Beatmap selectedBeatmap;
    @Setter
    private Consumer<Beatmap> onBeatmapSelectedListener;// will be used later for changing current BGM

    public BeatmapPane(ArrayList<Beatmap> beatmaps) {
        this.beatmaps = beatmaps;
        this.selectedBeatmap = beatmaps.isEmpty() ? null : beatmaps.get(0);

        this.getStyleClass().add("scroll-pane");
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setFitToWidth(true);

        initializeComponents();
        setupLayout();
        loadStyles();

        populateBeatmaps();
        handleEvent();
    }

    private void initializeComponents() {
        beatmapListBox = new VBox(2);
        beatmapListBox.setPadding(new Insets(5));
        beatmapListBox.getStyleClass().add("beatmap-list");
    }

    private void setupLayout() {
        this.setContent(beatmapListBox);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getHomeCssURL("BeatmapPane.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void populateBeatmaps() {
        if(beatmaps.isEmpty()) return;
        String currentOszPath = "";

        for(Beatmap beatmap: beatmaps) {
            // Container for the beatmap entry
            HBox entry = new HBox();
            entry.setPrefHeight(70);

            String oszPath = String.format("./src/main/resources/assets/beatmap/%s",
                    OsuParser.getOszPath(beatmap));
            if(!oszPath.equals(currentOszPath)) {
                System.out.println("different path, extracting bg");
                OsuParser.extractAndParse(beatmap);
                currentOszPath = oszPath;
            }else {
                System.out.println("same path, skipping extracting bg");
            }
//            System.out.println("bg for beatmap " + beatmap.getBeatmapSet().getTitle()
//                    + " " + OsuParser.getBgFile());
            BackgroundManager.setBeatmapBackground(entry);

            // Container for beatmap text info
            VBox textInfo = new VBox(2);
            textInfo.setPadding(new Insets(10, 0, 0, 10));
            textInfo.setPrefWidth(350);

            // Beatmap title
            Label titleLabel = new Label(beatmap.getBeatmapSet().getTitle());
            titleLabel.setTextFill(Color.WHITE);
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            // Artist // Creator
            String artist = String.format("%s // %s", beatmap.getBeatmapSet().getArtist(),
                    beatmap.getBeatmapSet().getCreator());
            Label artistLabel = new Label(artist);

            // Version
            Label versionLabel = new Label(beatmap.getVersion());

            // Stars display
            HBox starsBox = new HBox(2);
            for (int i = 0; i < beatmap.getStarRating(); i++) {
                Label star = new Label("★");
                star.setTextFill(Color.YELLOW);
                starsBox.getChildren().add(star);
            }

            entry.getStyleClass().add("beatmap-entry");
            titleLabel.getStyleClass().add("title");
            starsBox.getStyleClass().add("stars");
            textInfo.getChildren().addAll(titleLabel, artistLabel, versionLabel, starsBox);
            entry.getChildren().add(textInfo);

//            Rectangle overlay = new Rectangle();
//            overlay.setFill(new Color(0, 0, 0, 0.1));
//            StackPane map = new StackPane();
//            map.getChildren().addAll(overlay, entry);
            beatmapListBox.getChildren().add(entry);
        }

        // Select first beatmap by default if available
        if (!beatmapListBox.getChildren().isEmpty()) {
            beatmapListBox.getChildren().get(0).getStyleClass().add("selected");
        }
    }

    private void handleEvent() {
        beatmapListBox.setOnMouseClicked(e -> {
            Node clickedNode = e.getPickResult().getIntersectedNode();

            while (clickedNode != null && clickedNode.getParent() != beatmapListBox) {
                clickedNode = clickedNode.getParent();
            }

            if (clickedNode != null) {
                beatmapListBox.getChildren().forEach(node ->
                        node.getStyleClass().remove("selected"));
                clickedNode.getStyleClass().add("selected");

                int index = beatmapListBox.getChildren().indexOf(clickedNode);
                System.out.println("Clicked index: " + index);

                selectedBeatmap = beatmaps.get(index);

                if (onBeatmapSelectedListener != null) {
                    onBeatmapSelectedListener.accept(selectedBeatmap);
                }
            }
        });

        beatmapListBox.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            double width = getContent().getBoundsInLocal().getWidth();
            double vvalue = getVvalue();

            setVvalue(vvalue - deltaY / width);

            event.consume();
        });
    }
}
