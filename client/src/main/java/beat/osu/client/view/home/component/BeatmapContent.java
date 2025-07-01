package beat.osu.client.view.home.component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    private List<BeatmapCard> beatmapCards;
    private List<BeatmapCard> filteredBeatmapCards;
    private String currentFilter = "";
    @Getter
    private Beatmap selectedBeatmap;
    @Setter
    private Consumer<Beatmap> onBeatmapSelectedCallback;

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
                try {
                    OsuParser.parseBeatmap(beatmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                currentOszPath = oszPath;
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

    private void onBeatmapCardClicked(BeatmapCard clickedCard) {
        filteredBeatmapCards.forEach(card -> card.setSelected(false));

        clickedCard.setSelected(true);
        selectedBeatmap = clickedCard.getBeatmap();

        int index = filteredBeatmapCards.indexOf(clickedCard);
        System.out.println("Clicked index: " + index);

        if (onBeatmapSelectedCallback != null) {
            onBeatmapSelectedCallback.accept(selectedBeatmap);
        }
    }

    private boolean matchesQuery(Beatmap beatmap, String query) {
        return beatmap.getBeatmapSet().getTitle().toLowerCase().contains(query) ||
                beatmap.getBeatmapSet().getArtist().toLowerCase().contains(query);
    }

    public int filterBeatmaps(String query) {
        if (query == null || query.trim().isEmpty()) {
            clearFilter();
    } else {
            this.currentFilter = query.toLowerCase().trim();
            this.filteredBeatmapCards = beatmapCards.stream()
                    .filter(beatmapCard -> matchesQuery(beatmapCard.getBeatmap(), this.currentFilter))
                    .collect(Collectors.toList());
        }
        updateBeatmapCards();
        return filteredBeatmapCards.size();
    }

    public void clearFilter() {
        this.currentFilter = "";
        this.filteredBeatmapCards = this.beatmapCards;
    }

    public void clearContent() {
        beatmaps.clear();
        beatmapCards.clear();
        filteredBeatmapCards.clear();
        beatmapListBox.getChildren().clear();
        selectedBeatmap = null;
    }

    public void addBeatmap(Beatmap beatmap) {
        beatmaps.add(beatmap);
        
        BeatmapCard beatmapCard = new BeatmapCard(beatmap);
        beatmapCard.setOnClickCallback(this::onBeatmapCardClicked);
        
        beatmapCards.add(beatmapCard);
        
        if (currentFilter.isEmpty()) {
            filteredBeatmapCards = beatmapCards;
        } else {
            filteredBeatmapCards = beatmapCards.stream()
                    .filter(card -> matchesQuery(card.getBeatmap(), currentFilter))
                    .collect(Collectors.toList());
        }
        
        beatmapListBox.getChildren().clear();
        for (BeatmapCard card : filteredBeatmapCards) {
            beatmapListBox.getChildren().add(card);
        }
        
        if (selectedBeatmap == null && !filteredBeatmapCards.isEmpty()) {
            BeatmapCard firstCard = filteredBeatmapCards.get(0);
            firstCard.setSelected(true);
            selectedBeatmap = firstCard.getBeatmap();
            if (onBeatmapSelectedCallback != null) {
                onBeatmapSelectedCallback.accept(selectedBeatmap);
            }
        }
    }

    public VBox getBeatmapListBox() {
        return beatmapListBox;
    }
}
