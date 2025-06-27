package beat.osu.client.view.match.component.panels;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.ScreenManager;
import beat.osu.client.view.match.component.cards.MatchSlotCard;
import beat.osu.shared.dto.match.MatchPlayerDto;
import beat.osu.shared.enums.match.PlayerRole;
import beat.osu.shared.enums.match.PlayerStatus;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MatchSlotPanel extends VBox {

    private int maxPlayerCount;
    private List<MatchPlayerDto> players;

    private Label currentPlayersLabel;
    private VBox slotsContainer;
    private ScrollPane slotsScrollPane;
    private Map<Integer, MatchSlotCard> matchSlotCardsMap;
    private MatchSlotCard.SlotCardClickCallback slotCardClickCallback;

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
        VBox.setVgrow(slotsContainer, Priority.ALWAYS);

        slotsScrollPane = new ScrollPane(slotsContainer);
        slotsScrollPane.getStyleClass().add("slots-scroll-pane");
        slotsScrollPane.setFitToWidth(true);
        slotsScrollPane.setFitToHeight(true);
        slotsScrollPane.setPrefViewportWidth(Region.USE_COMPUTED_SIZE);
        VBox.setMargin(slotsScrollPane, new Insets(0, ScreenManager.SCREEN_WIDTH * 0.05, 0, ScreenManager.SCREEN_WIDTH * 0.022));

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
        for (int i = 0; i < maxPlayerCount; i++) {
            MatchSlotCard card = matchSlotCardsMap.get(i);
            if (card != null) {
                slotsContainer.getChildren().add(card);
            }
        }

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

    public void addPlayer(MatchPlayerDto player) {
        if (player == null) {
            return;
        }

        players.add(player);

        MatchSlotCard newCard = new MatchSlotCard(
                player.getId(),
                player.getMatchId(),
                player.getUser(),
                player.getRole(),
                player.getStatus(),
                player.getMatchSlotIndex()
        );
        if (slotCardClickCallback != null) {
            newCard.setSlotCardClickCallback(slotCardClickCallback);
        }

        MatchSlotCard oldCard = matchSlotCardsMap.get(player.getMatchSlotIndex());
        if (oldCard != null) {
            int cardIndex = slotsContainer.getChildren().indexOf(oldCard);
            if (cardIndex >= 0) {
                slotsContainer.getChildren().set(cardIndex, newCard);
            }
            matchSlotCardsMap.put(player.getMatchSlotIndex(), newCard);
        }

        updatePlayerCountLabel();
    }

    public void removePlayer(int userId) {
        MatchPlayerDto playerToRemove = null;
        for (MatchPlayerDto player : players) {
            if (player.getUser().getId() == userId) {
                playerToRemove = player;
                break;
            }
        }

        if (playerToRemove != null) {
            players.remove(playerToRemove);

            MatchSlotCard emptyCard = new MatchSlotCard(
                    -1,
                    -1,
                    null,
                    PlayerRole.PLAYER,
                    PlayerStatus.NOT_READY,
                    playerToRemove.getMatchSlotIndex()
            );
            if (slotCardClickCallback != null) {
                emptyCard.setSlotCardClickCallback(slotCardClickCallback);
            }

            MatchSlotCard oldCard = matchSlotCardsMap.get(playerToRemove.getMatchSlotIndex());
            if (oldCard != null) {
                int cardIndex = slotsContainer.getChildren().indexOf(oldCard);
                if (cardIndex >= 0) {
                    slotsContainer.getChildren().set(cardIndex, emptyCard);
                }
                matchSlotCardsMap.put(playerToRemove.getMatchSlotIndex(), emptyCard);
            }

            updatePlayerCountLabel();
        }
    }

    public void updatePlayer(MatchPlayerDto updatedPlayer) {
        if (updatedPlayer == null) {
            return;
        }

        for (int i = 0; i < players.size(); i++) {
            MatchPlayerDto player = players.get(i);
            if (player.getId() == updatedPlayer.getId()) {
                players.set(i, updatedPlayer);
                break;
            }
        }

        MatchSlotCard card = matchSlotCardsMap.get(updatedPlayer.getMatchSlotIndex());
        if (card != null) {
            card.updateCard(updatedPlayer.getUser(), updatedPlayer.getRole(), updatedPlayer.getStatus());
        }
    }

    private void updatePlayerCountLabel() {
        if (currentPlayersLabel != null) {
            currentPlayersLabel.setText("Current Players (" + players.size() + "/" + maxPlayerCount + ")");
        }
    }

    public void updateHost(int newHostUserId, int previousHostUserId) {
        if (players == null) {
            return;
        }

        for (MatchPlayerDto player : players) {
            if (player.getUserId() == newHostUserId) {
                player.setRole(PlayerRole.HOST);
            } else if (player.getUserId() == previousHostUserId) {
                player.setRole(PlayerRole.PLAYER);
            }
        }

        for (MatchPlayerDto player : players) {
            MatchSlotCard card = matchSlotCardsMap.get(player.getMatchSlotIndex());
            if (card != null) {
                card.updateCard(player.getUser(), player.getRole(), player.getStatus());
            }
        }
    }

    public void hostLeft(int previousHostUserId, int newHostUserId) {
        if (players == null) {
            return;
        }

        MatchPlayerDto hostToRemove = null;
        for (MatchPlayerDto player : players) {
            if (player.getUserId() == previousHostUserId) {
                hostToRemove = player;
                break;
            }
        }

        if (hostToRemove != null) {
            players.remove(hostToRemove);

            MatchSlotCard emptyCard = new MatchSlotCard(
                    -1,
                    -1,
                    null,
                    PlayerRole.PLAYER,
                    PlayerStatus.NOT_READY,
                    hostToRemove.getMatchSlotIndex()
            );

            MatchSlotCard oldCard = matchSlotCardsMap.get(hostToRemove.getMatchSlotIndex());
            if (oldCard != null) {
                slotsContainer.getChildren().remove(oldCard);

                int position = hostToRemove.getMatchSlotIndex();
                if (position < slotsContainer.getChildren().size()) {
                    slotsContainer.getChildren().add(position, emptyCard);
                } else {
                    slotsContainer.getChildren().add(emptyCard);
                }

                matchSlotCardsMap.put(hostToRemove.getMatchSlotIndex(), emptyCard);
            }
        }

        for (MatchPlayerDto player : players) {
            if (player.getUserId() == newHostUserId) {
                player.setRole(PlayerRole.HOST);
                MatchSlotCard card = matchSlotCardsMap.get(player.getMatchSlotIndex());
                if (card != null) {
                    card.updateCard(player.getUser(), player.getRole(), player.getStatus());
                }
            } else {
                player.setRole(PlayerRole.PLAYER);
                MatchSlotCard card = matchSlotCardsMap.get(player.getMatchSlotIndex());
                if (card != null) {
                    card.updateCard(player.getUser(), player.getRole(), player.getStatus());
                }
            }
        }

        updatePlayerCountLabel();
    }

    public void setSlotCardClickCallback(MatchSlotCard.SlotCardClickCallback callback) {
        this.slotCardClickCallback = callback;
        for (MatchSlotCard card : matchSlotCardsMap.values()) {
            card.setSlotCardClickCallback(callback);
        }
    }

    public boolean isUserHost(int userId) {
        return players.stream()
                .anyMatch(player -> player.getUserId() == userId && player.getRole() == PlayerRole.HOST);
    }

    public boolean isSlotEmpty(int slotIndex) {
        MatchSlotCard card = matchSlotCardsMap.get(slotIndex);
        return card != null && card.getUser() == null;
    }

    public void movePlayerToSlot(int userId, int oldSlotIndex, int newSlotIndex) {
        MatchPlayerDto playerToMove = null;
        for (MatchPlayerDto player : players) {
            if (player.getUserId() == userId) {
                playerToMove = player;
                break;
            }
        }

        if (playerToMove == null) {
            System.err.println("Player with ID " + userId + " not found for slot move");
            return;
        }

        playerToMove.setMatchSlotIndex(newSlotIndex);

        MatchSlotCard newCard = new MatchSlotCard(
                playerToMove.getId(),
                playerToMove.getMatchId(),
                playerToMove.getUser(),
                playerToMove.getRole(),
                playerToMove.getStatus(),
                newSlotIndex
        );
        if (slotCardClickCallback != null) {
            newCard.setSlotCardClickCallback(slotCardClickCallback);
        }

        MatchSlotCard emptyCard = new MatchSlotCard(
                -1,
                -1,
                null,
                PlayerRole.PLAYER,
                PlayerStatus.NOT_READY,
                oldSlotIndex
        );
        if (slotCardClickCallback != null) {
            emptyCard.setSlotCardClickCallback(slotCardClickCallback);
        }

        MatchSlotCard oldCardAtNewSlot = matchSlotCardsMap.get(newSlotIndex);
        MatchSlotCard oldCardAtOldSlot = matchSlotCardsMap.get(oldSlotIndex);
        
        if (oldCardAtNewSlot != null) {
            int newSlotCardIndex = slotsContainer.getChildren().indexOf(oldCardAtNewSlot);
            if (newSlotCardIndex >= 0) {
                slotsContainer.getChildren().set(newSlotCardIndex, newCard);
            }
        }
        
        if (oldCardAtOldSlot != null) {
            int oldSlotCardIndex = slotsContainer.getChildren().indexOf(oldCardAtOldSlot);
            if (oldSlotCardIndex >= 0) {
                slotsContainer.getChildren().set(oldSlotCardIndex, emptyCard);
            }
        }

        matchSlotCardsMap.put(newSlotIndex, newCard);
        matchSlotCardsMap.put(oldSlotIndex, emptyCard);
    }
}
