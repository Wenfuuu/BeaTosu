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
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;

public class BeatmapContent extends ScrollPane {

    private static final double BEATMAP_CARD_HEIGHT = ScreenManager.SCREEN_HEIGHT * 0.115;
    private static final double BASE_CARD_WIDTH = ScreenManager.SCREEN_WIDTH * 0.40;
    private static final double MAX_WIDTH_INCREASE = 72.0;
    private static final double WIDTH_CURVE_STRENGTH = 1.0;

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
    @Setter
    private Consumer<Beatmap> onBeatmapPlayCallback;

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
        this.setVbarPolicy(ScrollBarPolicy.NEVER);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);

        virtualContainer.prefWidthProperty().bind(this.widthProperty());
        virtualContainer.maxWidthProperty().bind(this.widthProperty());

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

        this.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            if (newBounds.getHeight() > 0) {
                applyCentermostScrollEffect();
            }
        });
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

        applyCentermostScrollEffect();
    }

    private void createAndPositionCard(int index) {
        Beatmap beatmap = filteredBeatmaps.get(index);
        
        try {
            OsuParser.parseBeatmap(beatmap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BeatmapCard card = new BeatmapCard(beatmap);
        double cardX = Math.max(0, this.getViewportBounds().getWidth() - BASE_CARD_WIDTH);
        card.setLayoutX(cardX);

        card.setOnClickCallback(this::onBeatmapCardClicked);

        if (selectedBeatmap != null && selectedBeatmap.equals(beatmap)) {
            card.setSelected(true);
        }
        
        card.setLayoutY(index * BEATMAP_CARD_HEIGHT);
        renderedCards.put(index, card);

        virtualContainer.getChildren().add(card);
    }

    private void onBeatmapCardClicked(BeatmapCard clickedCard) {
        boolean wasAlreadySelected = selectedBeatmap != null && selectedBeatmap.equals(clickedCard.getBeatmap());
        
        renderedCards.values().forEach(card -> card.setSelected(false));
        clickedCard.setSelected(true);
        selectedBeatmap = clickedCard.getBeatmap();

        if (wasAlreadySelected) {
            if (onBeatmapPlayCallback != null) {
                onBeatmapPlayCallback.accept(selectedBeatmap);
            }
        } else {
            if (onBeatmapSelectedCallback != null) {
                onBeatmapSelectedCallback.accept(selectedBeatmap);
            }
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

            Platform.runLater(() -> applyCentermostScrollEffect());
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
                if (selectedIndex == 0) {
                    this.setVvalue(0.0);
                    return;
                }

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

    public void setSelectedBeatmap(Beatmap beatmap) {
        if (beatmap != null && allBeatmaps.contains(beatmap)) {
            this.selectedBeatmap = beatmap;
        }
    }

    private void applyCentermostScrollEffect() {
        double viewportHeight = getViewportHeight();
        double viewportCenter = viewportHeight / 2.0;

        double scrollValue = this.getVvalue();
        double totalHeight = filteredBeatmaps.size() * BEATMAP_CARD_HEIGHT;
        double scrollTop = scrollValue * Math.max(0, totalHeight - viewportHeight);

        for (Map.Entry<Integer, BeatmapCard> entry : renderedCards.entrySet()) {
            int index = entry.getKey();
            BeatmapCard card = entry.getValue();

            double cardY = index * BEATMAP_CARD_HEIGHT - scrollTop;
            double cardCenter = cardY + BEATMAP_CARD_HEIGHT / 2.0;

            double distanceFromCenter = Math.abs(cardCenter - viewportCenter);

            double normalizedDistance = Math.min(1.0, distanceFromCenter / (viewportHeight / 2.0));

            double easingFactor = Math.pow(1.0 - normalizedDistance, WIDTH_CURVE_STRENGTH);
            double additionalWidth = MAX_WIDTH_INCREASE * easingFactor;
            double totalWidth = BASE_CARD_WIDTH + additionalWidth;

            card.setPrefWidth(totalWidth);
            card.setMinWidth(totalWidth);
            card.setMaxWidth(totalWidth);

            double cardX = Math.max(0, this.getViewportBounds().getWidth() - totalWidth);
            card.setLayoutX(cardX);
        }
    }
}
