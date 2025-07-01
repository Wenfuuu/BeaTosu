package beat.osu.client.view.home.component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.model.Beatmap;
import beat.osu.client.utils.OsuParser;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;

public class BeatmapContent extends ScrollPane {
    
    private static final double BEATMAP_CARD_HEIGHT = ScreenManager.SCREEN_HEIGHT * 0.11;

    private final Pane virtualContainer;
    private ArrayList<Beatmap> allBeatmaps;
    private List<Beatmap> filteredBeatmaps;
    private String currentFilter = "";
    
    private final Map<Integer, BeatmapCard> renderedCards = new HashMap<>();
    private int visibleItemCount = 0;
    private boolean hasTriggeredInitialSelection = false;
    
    @Getter
    private Beatmap selectedBeatmap;
    @Setter
    private Consumer<Beatmap> onBeatmapSelectedCallback;
    @Setter
    private BiConsumer<Beatmap, Boolean> onBeatmapSelectedWithBackgroundCallback;

    public BeatmapContent(ArrayList<Beatmap> beatmaps) {
        this.virtualContainer = new Pane();
        this.allBeatmaps = beatmaps;
        this.filteredBeatmaps = new ArrayList<>(beatmaps);
        this.selectedBeatmap = beatmaps.isEmpty() ? null : beatmaps.get(0);

        this.getStyleClass().add("scroll-pane");
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setFitToWidth(true);

        initializeComponents();
        setupLayout();
        loadStyles();
        setupVirtualScrolling();
    }

    private void initializeComponents() {
        virtualContainer.getStyleClass().add("beatmap-list");
        
        visibleItemCount = 10;
    }

    private void setupLayout() {
        this.setContent(virtualContainer);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getHomeCssURL("BeatmapContent.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void setupVirtualScrolling() {
        visibleItemCount = Math.max(10, (int) Math.ceil(ScreenManager.SCREEN_HEIGHT * 0.8 / BEATMAP_CARD_HEIGHT));
        updateVirtualContainerHeight();
        
        this.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            updateVisibleItems();
        });
        
        updateVisibleItems();
    }

    private void updateVirtualContainerHeight() {
        double totalHeight = filteredBeatmaps.size() * BEATMAP_CARD_HEIGHT;
        virtualContainer.setPrefHeight(totalHeight);
        virtualContainer.setMinHeight(totalHeight);
    }

    private void updateVisibleItems() {
        if (filteredBeatmaps.isEmpty()) {
            clearRenderedCards();
            return;
        }

        double scrollValue = this.getVvalue();
        
        double totalHeight = filteredBeatmaps.size() * BEATMAP_CARD_HEIGHT;
        double viewportHeight = getViewportHeight();
        double scrollTop = scrollValue * Math.max(0, totalHeight - viewportHeight);
        
        int firstVisibleItem = (int) (scrollTop / BEATMAP_CARD_HEIGHT);
        int newFirstIndex = Math.max(0, firstVisibleItem);
        int newLastIndex = Math.min(filteredBeatmaps.size() - 1, firstVisibleItem + visibleItemCount);
        
        renderedCards.entrySet().removeIf(entry -> {
            int index = entry.getKey();
            if (index < newFirstIndex || index > newLastIndex) {
                virtualContainer.getChildren().remove(entry.getValue());
                return true;
            }
            return false;
        });
        
        for (int i = newFirstIndex; i <= newLastIndex; i++) {
            if (!renderedCards.containsKey(i) && i < filteredBeatmaps.size()) {
                createAndPositionCard(i);
            }
        }
    }

    private void createAndPositionCard(int index) {
        Beatmap beatmap = filteredBeatmaps.get(index);
        
        try {
            OsuParser.parseBeatmap(beatmap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BeatmapCard card = new BeatmapCard(beatmap);
        card.setOnClickCallback(this::onBeatmapCardClicked);

        if (selectedBeatmap != null && selectedBeatmap.equals(beatmap)) {
            card.setSelected(true);
        }
        
        card.setLayoutY(index * BEATMAP_CARD_HEIGHT);
        renderedCards.put(index, card);
        virtualContainer.getChildren().add(card);
    }

    private void onBeatmapCardClicked(BeatmapCard clickedCard) {
        renderedCards.values().forEach(card -> card.setSelected(false));

        clickedCard.setSelected(true);
        selectedBeatmap = clickedCard.getBeatmap();

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
            return filteredBeatmaps.size();
        } else {
            this.currentFilter = query.toLowerCase().trim();
            this.filteredBeatmaps = allBeatmaps.stream()
                    .filter(beatmap -> matchesQuery(beatmap, this.currentFilter))
                    .collect(Collectors.toList());
        }
        
        renderedCards.clear();
        virtualContainer.getChildren().clear();
        updateVirtualContainerHeight();
        updateVisibleItems();
        
        return filteredBeatmaps.size();
    }

    public void clearFilter() {
        this.currentFilter = "";
        this.filteredBeatmaps = new ArrayList<>(this.allBeatmaps);
        
        renderedCards.clear();
        virtualContainer.getChildren().clear();
        updateVirtualContainerHeight();
        updateVisibleItems();
    }

    public void clearContent() {
        allBeatmaps.clear();
        filteredBeatmaps.clear();
        renderedCards.clear();
        virtualContainer.getChildren().clear();
        selectedBeatmap = null;
        hasTriggeredInitialSelection = false;
        updateVirtualContainerHeight();
    }

    private void clearRenderedCards() {
        renderedCards.values().forEach(card -> {
            virtualContainer.getChildren().remove(card);
        });
        renderedCards.clear();
    }

    private double getViewportHeight() {
        return this.getBoundsInLocal().getHeight();
    }

    public void triggerInitialSelection() {
        if (!filteredBeatmaps.isEmpty() && selectedBeatmap != null && !hasTriggeredInitialSelection) {
            System.out.println("Triggering initial selection for beatmap: " + selectedBeatmap.getBeatmapSet().getTitle());
            hasTriggeredInitialSelection = true;
            
            scrollToSelected();
            
            if (onBeatmapSelectedWithBackgroundCallback != null) {
                onBeatmapSelectedWithBackgroundCallback.accept(selectedBeatmap, false);
            } else if (onBeatmapSelectedCallback != null) {
                onBeatmapSelectedCallback.accept(selectedBeatmap);
            } else {
                System.out.println("Warning: no beatmap selection callbacks are set!");
            }
            
            for (BeatmapCard card : renderedCards.values()) {
                if (card.getBeatmap().equals(selectedBeatmap)) {
                    card.setSelected(true);
                    break;
                }
            }
        } else {
            System.out.println("Skipping initial selection - isEmpty: " + filteredBeatmaps.isEmpty() + 
                             ", selectedBeatmap null: " + (selectedBeatmap == null) + 
                             ", already triggered: " + hasTriggeredInitialSelection);
        }
    }
    
    public void scrollToSelected() {
        if (selectedBeatmap != null) {
            int selectedIndex = -1;
            for (int i = 0; i < filteredBeatmaps.size(); i++) {
                if (filteredBeatmaps.get(i).equals(selectedBeatmap)) {
                    selectedIndex = i;
                    break;
                }
            }
            
            if (selectedIndex >= 0) {
                double totalHeight = filteredBeatmaps.size() * BEATMAP_CARD_HEIGHT;
                double viewportHeight = getViewportHeight();
                double targetY = selectedIndex * BEATMAP_CARD_HEIGHT + 60;
                
                targetY = Math.max(0, targetY - viewportHeight / 2);
                
                double maxScrollY = Math.max(0, totalHeight - viewportHeight);
                double scrollValue = maxScrollY > 0 ? targetY / maxScrollY : 0;
                scrollValue = Math.max(0, Math.min(1, scrollValue));
                
                this.setVvalue(scrollValue);
            }
        }
    }
    
    public void resetSelectionState() {
        hasTriggeredInitialSelection = false;
    }
}
