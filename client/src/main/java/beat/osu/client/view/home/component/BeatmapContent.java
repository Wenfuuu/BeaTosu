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
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.function.Consumer;

public class BeatmapContent extends ScrollPane {
    private final VBox beatmapListBox;
    private ArrayList<Beatmap> beatmaps;
    @Getter
    private Beatmap selectedBeatmap;
    @Setter
    private Consumer<Beatmap> onBeatmapSelectedCallback;// will be used later for changing current BGM

    public BeatmapContent(ArrayList<Beatmap> beatmaps) {
        this.beatmapListBox = new VBox();
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
        beatmapListBox.getStyleClass().add("beatmap-list");
    }

    private void setupLayout() {
        this.setContent(beatmapListBox);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getHomeCssURL("BeatmapContent.css");
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
            StackPane beatmapContainer = new StackPane();
            beatmapContainer.setPrefHeight(70);
            beatmapContainer.getStyleClass().add("beatmap-container");

            String oszPath = OsuParser.getOszPath(beatmap);
            if(!oszPath.equals(currentOszPath)) {
                System.out.println("different path, parsing beatmap");
//                OsuParser.extractAndParse(beatmap);
                try {
                    OsuParser.parseBeatmap(beatmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                currentOszPath = oszPath;
            }else {
                System.out.println("same path, skipping parsing beatmap");
            }

            HBox backgroundLayer = new HBox();
            backgroundLayer.setPrefHeight(70);
            BackgroundManager.setBeatmapBackground(backgroundLayer);

            HBox overlayLayer = new HBox();
            overlayLayer.setPrefHeight(70);
            overlayLayer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

            HBox contentLayer = new HBox();
            contentLayer.setPrefHeight(70);
            contentLayer.setPickOnBounds(false);

            VBox textInfo = new VBox(2);
            textInfo.setPadding(new Insets(10, 0, 0, 10));
            textInfo.setPrefWidth(350);

            Label titleLabel = new Label(beatmap.getBeatmapSet().getTitle());

            String artist = String.format("%s // %s", beatmap.getBeatmapSet().getArtist(),
                    beatmap.getBeatmapSet().getCreator());
            Label artistLabel = new Label(artist);

            Label versionLabel = new Label(beatmap.getVersion());

            HBox starsBox = new HBox(2);
            for (int i = 0; i < beatmap.getStarRating(); i++) {
                Label star = new Label("★");
                starsBox.getChildren().add(star);
            }

            contentLayer.getStyleClass().add("beatmap-content");
            titleLabel.getStyleClass().add("title");
            textInfo.getChildren().addAll(titleLabel, artistLabel, versionLabel, starsBox);
            contentLayer.getChildren().add(textInfo);

            beatmapContainer.getChildren().addAll(backgroundLayer, overlayLayer, contentLayer);
            beatmapListBox.getChildren().add(beatmapContainer);
        }

        // Select first beatmap by default if available
        if (!beatmapListBox.getChildren().isEmpty()) {
            beatmapListBox.getChildren().get(0).getStyleClass().add("selected");
            selectedBeatmap = beatmaps.get(0);

//            OsuParser.extractAndParse(selectedBeatmap);
            try {
                OsuParser.parseBeatmap(selectedBeatmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleEvent() {
        beatmapListBox.setOnMouseClicked(e -> {
            Node clickedNode = e.getPickResult().getIntersectedNode();

            while (clickedNode != null &&
                    (!(clickedNode instanceof StackPane) || clickedNode.getParent() != beatmapListBox)) {
                clickedNode = clickedNode.getParent();
            }

            if (clickedNode != null) {
                beatmapListBox.getChildren().forEach(node ->
                        node.getStyleClass().remove("selected"));
                clickedNode.getStyleClass().add("selected");

                int index = beatmapListBox.getChildren().indexOf(clickedNode);
                System.out.println("Clicked index: " + index);

                selectedBeatmap = beatmaps.get(index);

                if (onBeatmapSelectedCallback != null) {
                    onBeatmapSelectedCallback.accept(selectedBeatmap);
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
