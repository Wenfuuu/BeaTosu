package beat.osu.client.view.lobby.component.panels;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beat.osu.client.controller.MatchController;
import beat.osu.client.helper.CssManager;
import beat.osu.client.view.lobby.component.cards.MatchCard;
import beat.osu.client.view.lobby.component.ui.MatchFilters;
import beat.osu.shared.dto.match.MatchDto;
import beat.osu.shared.dto.match.events.*;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Setter;

public class MatchesPanel extends VBox {

    @FunctionalInterface
    public interface MatchCardClickCallback {
        void onMatchCardClicked(MatchCard matchCard);
    }

    private MatchFilters matchFilters;

    private final List<MatchCard> matchCards;
    private Map<Integer, MatchCard> matchCardMap;
    private VBox matchesContainer;
    private ScrollPane matchesScrollPane;

    private MatchController matchController;

    @Setter
    private MatchCardClickCallback matchCardClickCallback;

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

        matchesContainer = new VBox();
        matchesContainer.getStyleClass().add("matches-container");

        matchesScrollPane = new ScrollPane(matchesContainer);
        VBox.setMargin(matchesScrollPane, new Insets(8, 0, 0, 12));
        matchesScrollPane.getStyleClass().add("matches-scroll-pane");
    }

    private void setLayout() {
        this.getChildren().addAll(matchFilters, matchesScrollPane);
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
            matchCard.setOpacity(0);
            matchesContainer.getChildren().add(matchCard);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), matchCard);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
    }

    public void loadInitialMatches() {
        Platform.runLater(() -> {
            List<MatchDto> matches = matchController.getMatches();
            for (MatchDto match : matches) {
                addMatch(match);
            }
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
            fadeOut.setOnFinished(e -> matchesContainer.getChildren().remove(matchCard));
            fadeOut.play();
        }
    }
}
