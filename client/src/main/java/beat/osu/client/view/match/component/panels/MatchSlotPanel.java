package beat.osu.client.view.match.component.panels;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beat.osu.client.helper.CssManager;
import beat.osu.client.view.match.component.cards.MatchSlotCard;
import beat.osu.shared.dto.match.MatchPlayerDto;
import beat.osu.shared.enums.match.PlayerRole;
import beat.osu.shared.enums.match.PlayerStatus;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MatchSlotPanel extends VBox {

    private int maxPlayerCount;
    private List<MatchPlayerDto> players;

    private Label currentPlayersLabel;
    private VBox slotsContainer;
    private ScrollPane slotsScrollPane;
    private Map<Integer, MatchSlotCard> matchSlotCardsMap;

    public MatchSlotPanel(int maxPlayerCount, List<MatchPlayerDto> players) {
        this.maxPlayerCount = maxPlayerCount;
        this.players = players;

        initializeComponents();
        setLayout();
        loadStyles();
    }

    private void initializeComponents() {
        this.getStyleClass().add("match-slot-panel");

        currentPlayersLabel = new Label("Current Players (" + players.size() + "/" + maxPlayerCount + ")");
        currentPlayersLabel.getStyleClass().add("current-players-label");
        VBox.setMargin(currentPlayersLabel, new Insets(0, 0, 0, 72));

        slotsContainer = new VBox();
        slotsContainer.getStyleClass().add("slots-container");

        slotsScrollPane = new ScrollPane(slotsContainer);
        slotsScrollPane.getStyleClass().add("slots-scroll-pane");
        slotsScrollPane.setFitToWidth(true);
        slotsScrollPane.setFitToHeight(true);
        slotsScrollPane.setPrefViewportWidth(Region.USE_COMPUTED_SIZE);
        slotsScrollPane.setMaxWidth(Region.USE_PREF_SIZE);
        VBox.setMargin(slotsScrollPane, new Insets(0, 0, 0, 44));

        matchSlotCardsMap = new HashMap<>();

        for (int i = 0; i < maxPlayerCount; i++) {
            MatchSlotCard emptyCard = new MatchSlotCard(
                    i,
                    -1,
                    null,
                    PlayerRole.PLAYER,
                    PlayerStatus.NOT_READY,
                    i
            );
            matchSlotCardsMap.put(i, emptyCard);
        }

        for (MatchPlayerDto player : players) {
            MatchSlotCard card = new MatchSlotCard(
                    player.getId(),
                    player.getMatchId(),
                    player.getUser(),
                    player.getRole(),
                    player.getStatus(),
                    player.getMatchSlotIndex()
            );
            matchSlotCardsMap.put(player.getMatchSlotIndex(), card);
        }
    }

    private void setLayout() {
        slotsContainer.getChildren().addAll(matchSlotCardsMap.values());

        this.getChildren().addAll(currentPlayersLabel, slotsScrollPane);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getMatchCssURL("MatchSlotPanel.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }
}
