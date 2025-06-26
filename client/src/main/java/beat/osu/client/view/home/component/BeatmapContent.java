package beat.osu.client.view.home.component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.function.Consumer;

import beat.osu.client.helper.CssManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.utils.OsuParser;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

public class BeatmapContent extends ScrollPane {
    private final VBox beatmapListBox;
    private ArrayList<Beatmap> beatmaps;
    private ArrayList<BeatmapCard> beatmapCards;
    @Getter
    private Beatmap selectedBeatmap;
    @Setter
    private Consumer<Beatmap> onBeatmapSelectedCallback;// will be used later for changing current BGM

    public BeatmapContent(ArrayList<Beatmap> beatmaps) {
        this.beatmapListBox = new VBox();
        this.beatmaps = beatmaps;
        this.beatmapCards = new ArrayList<>();
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
            String oszPath = OsuParser.getOszPath(beatmap);
            if(!oszPath.equals(currentOszPath)) {
                System.out.println("different path, parsing beatmap");
                try {
                    OsuParser.parseBeatmap(beatmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                currentOszPath = oszPath;
            } else {
                System.out.println("same path, skipping parsing beatmap");
            }

            BeatmapCard beatmapCard = new BeatmapCard(beatmap);
            beatmapCard.setOnClickCallback(this::onBeatmapCardClicked);
            
            beatmapCards.add(beatmapCard);
            beatmapListBox.getChildren().add(beatmapCard);
        }

        // Select first beatmap by default if available
        if (!beatmapCards.isEmpty()) {
            beatmapCards.get(0).setSelected(true);
            selectedBeatmap = beatmaps.get(0);

            try {
                OsuParser.parseBeatmap(selectedBeatmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleEvent() {
        beatmapListBox.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            double width = getContent().getBoundsInLocal().getWidth();
            double vvalue = getVvalue();

            setVvalue(vvalue - deltaY / width);

            event.consume();
        });
    }

    private void onBeatmapCardClicked(BeatmapCard clickedCard) {
        // Deselect all cards
        beatmapCards.forEach(card -> card.setSelected(false));
        
        // Select the clicked card
        clickedCard.setSelected(true);
        
        // Update selected beatmap
        selectedBeatmap = clickedCard.getBeatmap();
        
        // Find index for logging
        int index = beatmapCards.indexOf(clickedCard);
        System.out.println("Clicked index: " + index);

        // Trigger callback if set
        if (onBeatmapSelectedCallback != null) {
            onBeatmapSelectedCallback.accept(selectedBeatmap);
        }
    }
}
