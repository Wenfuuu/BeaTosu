package beat.osu.client.view.home.component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import beat.osu.client.helper.CssManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.Song;
import beat.osu.client.utils.OsuParser;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

public class BeatmapContent extends ScrollPane {
    private final VBox beatmapListBox;
    private ArrayList<Beatmap> beatmaps;
    private List<BeatmapCard> beatmapCards;
    private List<BeatmapCard> filteredBeatmapCards;
    private String currentFilter = "";
    @Getter
    private Beatmap selectedBeatmap;
    @Setter
    private Consumer<Beatmap> onBeatmapSelectedCallback;// will be used later for changing current BGM

    public BeatmapContent(ArrayList<Beatmap> beatmaps) {
        this.beatmapListBox = new VBox();
        this.beatmaps = beatmaps;
        this.beatmapCards = new ArrayList<>();
        this.filteredBeatmapCards = new ArrayList<>();
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
        if (beatmaps.isEmpty())
            return;
        String currentOszPath = "";

        for (Beatmap beatmap : beatmaps) {
            String oszPath = OsuParser.getOszPath(beatmap);
            if (!oszPath.equals(currentOszPath)) {
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
        }

        filteredBeatmapCards = beatmapCards;
        updateBeatmapCards();
    }

    public void updateBeatmapCards() {
        // Clear existing children to prevent duplicate additions
        beatmapListBox.getChildren().clear();

        for (BeatmapCard beatmapCard : filteredBeatmapCards) {
            beatmapListBox.getChildren().add(beatmapCard);
        }

        // Select first beatmap by default if available
        if (!filteredBeatmapCards.isEmpty()) {
            filteredBeatmapCards.get(0).setSelected(true);
            selectedBeatmap = filteredBeatmapCards.get(0).getBeatmap();
            onBeatmapCardClicked(filteredBeatmapCards.get(0));

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
        filteredBeatmapCards.forEach(card -> card.setSelected(false));

        // Select the clicked card
        clickedCard.setSelected(true);

        // Update selected beatmap
        selectedBeatmap = clickedCard.getBeatmap();

        // Find index for logging
        int index = filteredBeatmapCards.indexOf(clickedCard);
        System.out.println("Clicked index: " + index);

        // Trigger callback if set
        if (onBeatmapSelectedCallback != null) {
            onBeatmapSelectedCallback.accept(selectedBeatmap);
        }
    }

    private boolean matchesQuery(Beatmap beatmap, String query) {
        return beatmap.getBeatmapSet().getTitle().toLowerCase().contains(query) ||
                beatmap.getBeatmapSet().getArtist().toLowerCase().contains(query);
    }

    public void filterBeatmaps(String query) {
        if (query == null || query.trim().isEmpty()) {
            clearFilter();
        } else {
            this.currentFilter = query.toLowerCase().trim();
            this.filteredBeatmapCards = beatmapCards.stream()
                    .filter(beatmapCard -> matchesQuery(beatmapCard.getBeatmap(), this.currentFilter))
                    .collect(Collectors.toList());
        }
        updateBeatmapCards();
    }

    public void clearFilter() {
        this.currentFilter = "";
        this.filteredBeatmapCards = this.beatmapCards;
    }
}
