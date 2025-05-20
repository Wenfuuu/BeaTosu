package beat.osu.beatosu.view.home.component;

import beat.osu.beatosu.helper.CssManager;
import beat.osu.beatosu.model.Beatmap;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
        this.selectedBeatmap = beatmaps.isEmpty() ? null : beatmaps.getFirst();

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
        for(Beatmap beatmap: beatmaps) {
            // Container for the beatmap entry
            HBox entry = new HBox();
            entry.setPrefHeight(70);
            entry.setStyle("-fx-background-color: #993300;");

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

            beatmapListBox.getChildren().add(entry);
        }

        // Select first beatmap by default if available
        if (!beatmapListBox.getChildren().isEmpty()) {
            beatmapListBox.getChildren().getFirst().getStyleClass().add("selected");
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
