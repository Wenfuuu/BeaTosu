package beat.osu.client.view.lobby.component.panels;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import beat.osu.client.controller.MatchController;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.view.lobby.component.cards.MatchCard;
import beat.osu.client.view.lobby.component.ui.MatchFilters;
import beat.osu.shared.dto.match.MatchDto;
import beat.osu.shared.dto.match.events.HostChangedEvent;
import beat.osu.shared.dto.match.events.HostLeftEvent;
import beat.osu.shared.dto.match.events.MatchBeatmapUpdatedEvent;
import beat.osu.shared.dto.match.events.MatchCompletedEvent;
import beat.osu.shared.dto.match.events.MatchCreatedEvent;
import beat.osu.shared.dto.match.events.MatchEndedEvent;
import beat.osu.shared.dto.match.events.MatchNameUpdatedEvent;
import beat.osu.shared.dto.match.events.MatchStartedEvent;
import beat.osu.shared.dto.match.events.PlayerKickedEvent;
import beat.osu.shared.dto.match.events.SlotChangedEvent;
import beat.osu.shared.dto.match.events.UserJoinedMatchEvent;
import beat.osu.shared.dto.match.events.UserLeftMatchEvent;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Setter;

public class MatchesPanel extends VBox {

    @FunctionalInterface
    public interface MatchCardClickCallback {
        void onMatchCardClicked(MatchCard matchCard);
    }

    @FunctionalInterface
    public interface MatchCountUpdateCallback {
        void onMatchCountUpdated(int filteredCount, int totalCount);
    }

    private MatchFilters matchFilters;

    private final List<MatchCard> matchCards;
    private Map<Integer, MatchCard> matchCardMap;
    private VBox matchesContainer;
    private ScrollPane matchesScrollPane;
    private Label noMatchesLabel;

    private MatchController matchController;

    @Setter
    private MatchCardClickCallback matchCardClickCallback;
    
    @Setter
    private MatchCountUpdateCallback matchCountUpdateCallback;

    public MatchesPanel(MatchController matchController) {
        this.matchController = matchController;
        this.matchCards = new ArrayList<>();
        this.matchCardMap = new HashMap<>();

        initializeComponents();
        setLayout();
        loadStyles();
        setupMatchCallbacks();
    }

    private void initializeComponents() {
        this.getStyleClass().add("matches-panel");

        matchFilters = new MatchFilters();
        setupFilterCallbacks();

        matchesContainer = new VBox();
        matchesContainer.getStyleClass().add("matches-container");

        matchesScrollPane = new ScrollPane(matchesContainer);
        VBox.setMargin(matchesScrollPane, new Insets(8, 0, 0, 12));
        matchesScrollPane.getStyleClass().add("matches-scroll-pane");

        noMatchesLabel = new Label("There are no matches available.\nClick 'New Game' to start a new game!");
        noMatchesLabel.getStyleClass().add("no-matches-label");
        noMatchesLabel.setAlignment(Pos.CENTER);
        noMatchesLabel.setVisible(false);
        VBox.setMargin(noMatchesLabel, new Insets(ScreenManager.SCREEN_HEIGHT * 0.14, 0, 0, 0));
    }

    private void setLayout() {
        this.getChildren().addAll(matchFilters, matchesScrollPane, noMatchesLabel);
    }

    private void loadStyles() {
        try {
            URL cssUrl = CssManager.getLobbyCssURL("MatchesPanel.css");
            if (cssUrl != null) {
                this.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Could not load MatchesPanel CSS: " + e.getMessage());
        }
    }

    private void addMatch(MatchDto match) {
        if (matchCardMap.containsKey(match.getId())) {
            return;
        }
        
        MatchCard matchCard = new MatchCard(
            match.getId(),
            match.getName(),
            match.getPassword(),
            match.isInProgress(),
            match.getMaxPlayerCount(),
            match.getBeatmap().getId(),
            match.getBeatmap().getBeatmapSetDto().getTitle(),
            match.getBeatmap().getBeatmapSetId(),
            match.getLowestRank(),
            match.getHighestRank(),
            match.getWinCondition(),
            match.getPlayers()
        );

        matchCard.setOnMouseClicked(e -> {
            if (matchCardClickCallback != null) {
                matchCardClickCallback.onMatchCardClicked(matchCard);
            }
        });
        
        matchCards.add(matchCard);
        matchCardMap.put(match.getId(), matchCard);

        Platform.runLater(() -> {
            if (shouldShowMatch(matchCard)) {
                matchCard.setOpacity(0);
                matchesContainer.getChildren().add(matchCard);

                updateNoMatchesMessageVisibility();
                notifyMatchCountUpdate();

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), matchCard);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            } else {
                updateNoMatchesMessageVisibility();
                notifyMatchCountUpdate();
            }
        });
    }

    public void loadInitialMatches() {
        Platform.runLater(() -> {
            List<MatchDto> matches = matchController.getMatches();
            for (MatchDto match : matches) {
                addMatch(match);
            }
            updateNoMatchesMessageVisibility();
            notifyMatchCountUpdate();
        });
    }
    
    private void setupMatchCallbacks() {
        matchController.addMatchCreatedCallback(this::onMatchCreated);
        matchController.addUserJoinedMatchCallback(this::onUserJoinedMatch);
        matchController.addUserLeftMatchCallback(this::onUserLeftMatch);
        matchController.addPlayerKickedCallback(this::onPlayerKicked);
        matchController.addMatchEndedCallback(this::onMatchEnded);
        matchController.addHostChangedCallback(this::onHostChanged);
        matchController.addHostLeftCallback(this::onHostLeft);
        matchController.addSlotChangedCallback(this::onSlotChanged);
        matchController.addMatchNameUpdatedCallback(this::onMatchNameUpdated);
        matchController.addMatchBeatmapUpdatedCallback(this::onMatchBeatmapUpdated);
        matchController.addMatchStartedCallback(this::onMatchStarted);
        matchController.addMatchCompletedCallback(this::onMatchCompleted);
    }

    private void onMatchCompleted(MatchCompletedEvent event) {
        Platform.runLater(() -> {
            MatchCard matchCard = matchCardMap.get(event.getMatchId());
            if (matchCard != null) {
                matchCard.updateMatchInProgressStatus(false);
            }
        });
    }

    private void onMatchStarted(MatchStartedEvent event) {
        Platform.runLater(() -> {
            MatchCard matchCard = matchCardMap.get(event.getMatchId());
            if (matchCard != null) {
                matchCard.updateMatchInProgressStatus(true);
            }
        });
    }
    
    private void onMatchCreated(MatchCreatedEvent event) {
        Platform.runLater(() -> addMatch(event.getMatch()));
    }
    
    private void onUserJoinedMatch(UserJoinedMatchEvent event) {
        Platform.runLater(() -> {
            MatchCard matchCard = matchCardMap.get(event.getMatchId());
            matchCard.addPlayer(event.getMatchPlayer());
        });
    }
    
    private void onUserLeftMatch(UserLeftMatchEvent event) {
        Platform.runLater(() -> {
            MatchCard matchCard = matchCardMap.get(event.getMatchId());
            if (matchCard != null) {
                matchCard.removePlayer(event.getUserId());
            }
        });
    }
    
    private void onPlayerKicked(PlayerKickedEvent event) {
        Platform.runLater(() -> {
            MatchCard matchCard = matchCardMap.get(event.getMatchId());
            if (matchCard != null) {
                matchCard.removePlayer(event.getKickedUserId());
            }
        });
    }
    
    private void onMatchEnded(MatchEndedEvent event) {
        Platform.runLater(() -> {
            removeMatch(event.getMatchId());
        });
    }
    
    private void onHostChanged(HostChangedEvent event) {
        Platform.runLater(() -> {
            MatchCard matchCard = matchCardMap.get(event.getMatchId());
            if (matchCard != null) {
                matchCard.updateHost(event.getNewHostUserId(), event.getPreviousHostUserId());
            }
        });
    }
    
    private void onHostLeft(HostLeftEvent event) {
        Platform.runLater(() -> {
            MatchCard matchCard = matchCardMap.get(event.getMatchId());
            if (matchCard != null) {
                matchCard.hostLeft(event.getPreviousHostUserId(), event.getNewHostUserId());
            }
        });
    }
    
    private void onSlotChanged(SlotChangedEvent event) {
        Platform.runLater(() -> {
            MatchCard matchCard = matchCardMap.get(event.getMatchId());
            if (matchCard != null) {
                matchCard.movePlayerToSlot(event.getUserId(), event.getOldSlotIndex(), event.getNewSlotIndex());
            }
        });
    }

    private void onMatchNameUpdated(MatchNameUpdatedEvent event) {
        Platform.runLater(() -> {
            MatchCard matchCard = matchCardMap.get(event.getMatchId());
            if (matchCard != null) {
                matchCard.updateMatchName(event.getNewName());
            }
        });
    }

    private void onMatchBeatmapUpdated(MatchBeatmapUpdatedEvent event) {
        Platform.runLater(() -> {
            MatchCard matchCard = matchCardMap.get(event.getMatchId());
            if (matchCard != null) {
                String beatmapName = event.getNewBeatmapDto().getBeatmapSetDto().getTitle();
                matchCard.updateBeatmap(event.getNewBeatmapDto().getId(), beatmapName);
            }
        });
    }
    
    private void removeMatch(int matchId) {
        MatchCard matchCard = matchCardMap.get(matchId);
        if (matchCard == null) {
            return;
        }
        
        matchCards.remove(matchCard);
        matchCardMap.remove(matchId);
        
        if (matchesContainer.getChildren().contains(matchCard)) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), matchCard);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                matchesContainer.getChildren().remove(matchCard);
                updateNoMatchesMessageVisibility();
                notifyMatchCountUpdate();
            });
            fadeOut.play();
        } else {
            updateNoMatchesMessageVisibility();
            notifyMatchCountUpdate();
        }
    }
    
    private void setupFilterCallbacks() {
        matchFilters.getOwnedBeatmapsCheckBox().setOnAction(e -> applyFilters());
        matchFilters.getShowFullCheckBox().setOnAction(e -> applyFilters());
        matchFilters.getShowLockedCheckBox().setOnAction(e -> applyFilters());
        matchFilters.getShowInProgressCheckBox().setOnAction(e -> applyFilters());
        
        matchFilters.getSearchTextField().textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        Platform.runLater(() -> {
            matchesContainer.getChildren().clear();
            
            for (MatchCard matchCard : matchCards) {
                if (shouldShowMatch(matchCard)) {
                    matchesContainer.getChildren().add(matchCard);
                }
            }
            updateNoMatchesMessageVisibility();
            notifyMatchCountUpdate();
        });
    }
    
    private void updateNoMatchesMessageVisibility() {
        boolean hasVisibleMatches = !matchesContainer.getChildren().isEmpty();
        noMatchesLabel.setVisible(!hasVisibleMatches);
        noMatchesLabel.setManaged(!hasVisibleMatches);
    }

    private boolean shouldShowMatch(MatchCard matchCard) {
        if (!matchFilters.getOwnedBeatmapsCheckBox().isSelected() && !matchCard.userHasBeatmap()) {
            return false;
        }
        
        if (!matchFilters.getShowFullCheckBox().isSelected() && matchCard.isFull()) {
            return false;
        }
        
        if (!matchFilters.getShowLockedCheckBox().isSelected() && matchCard.isLocked()) {
            return false;
        }
        
        if (!matchFilters.getShowInProgressCheckBox().isSelected() && matchCard.isInProgress()) {
            return false;
        }
        
        String searchText = matchFilters.getSearchTextField().getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lowerSearchText = searchText.toLowerCase().trim();
            String matchName = matchCard.getMatchName();
            return matchName != null && matchName.toLowerCase().contains(lowerSearchText);
        }
        
        return true;
    }
    
    public List<MatchCard> getSuitableMatchesForQuickJoin() {
        return matchCards.stream()
            .filter(matchCard -> {
                boolean hasNoPassword = !matchCard.hasPassword();
                boolean userHasBeatmap = matchCard.userHasBeatmap();
                boolean notInProgress = !matchCard.isInProgress();
                boolean notFull = !matchCard.isFull();

                return hasNoPassword && userHasBeatmap && notInProgress && notFull;
            })
            .collect(Collectors.toList());
    }
    
    public int getTotalMatchCount() {
        return matchCards.size();
    }
    
    public int getFilteredMatchCount() {
        return matchesContainer.getChildren().size();
    }
    
    private void notifyMatchCountUpdate() {
        if (matchCountUpdateCallback != null) {
            matchCountUpdateCallback.onMatchCountUpdated(getFilteredMatchCount(), getTotalMatchCount());
        }
    }
}
