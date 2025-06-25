package beat.osu.client.view.lobby.component.panels;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import beat.osu.client.controller.MatchController;
import beat.osu.client.helper.CssManager;
import beat.osu.client.view.lobby.component.cards.MatchCard;
import beat.osu.client.view.lobby.component.ui.MatchFilters;
import beat.osu.shared.dto.match.MatchDto;
import beat.osu.shared.dto.match.MatchPlayerDto;
import beat.osu.shared.dto.match.events.MatchCreatedEvent;
import beat.osu.shared.dto.match.events.PlayerKickedEvent;
import beat.osu.shared.dto.match.events.UserJoinedMatchEvent;
import beat.osu.shared.dto.match.events.UserLeftMatchEvent;
import beat.osu.shared.dto.user.UserDto;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MatchesPanel extends VBox {

    private MatchFilters matchFilters;

    private final List<MatchCard> matchCards;
    private Map<Integer, MatchCard> matchCardMap;
    private VBox matchesContainer;
    private ScrollPane matchesScrollPane;

    private MatchController matchController;

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
            match.getStatus(),
            match.getMaxPlayerCount(),
            match.getBeatmapId(),
            match.getBeatmapName(),
            match.getLowestRank(),
            match.getHighestRank(),
            match.getWinCondition(),
            match.getPlayers()
        );
        
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

    private void loadInitialMatches() {
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
        
        loadInitialMatches();
    }
    
    private void onMatchCreated(MatchCreatedEvent event) {
        Platform.runLater(() -> addMatch(event.getMatch()));
    }
    
    private void onUserJoinedMatch(UserJoinedMatchEvent event) {
        Platform.runLater(() -> {
            MatchCard matchCard = matchCardMap.get(event.getMatchId());
            if (matchCard != null) {
                matchCard.updatePlayerCount(matchCard.getPlayerCount() + 1);
            }
        });
    }
    
    private void onUserLeftMatch(UserLeftMatchEvent event) {
        Platform.runLater(() -> {
            MatchCard matchCard = matchCardMap.get(event.getMatchId());
            if (matchCard != null) {
                matchCard.updatePlayerCount(matchCard.getPlayerCount() - 1);
            }
        });
    }
    
    private void onPlayerKicked(PlayerKickedEvent event) {
        Platform.runLater(() -> {
            MatchCard matchCard = matchCardMap.get(event.getMatchId());
            if (matchCard != null) {
                matchCard.updatePlayerCount(matchCard.getPlayerCount() - 1);
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
