package beat.osu.client.view.lobby.component.cards;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import beat.osu.client.Main;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.LocaleManager;
import beat.osu.shared.dto.match.MatchPlayerDto;
import beat.osu.shared.enums.match.PlayerRole;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class MatchCard extends HBox {

    @Getter
    private Integer matchId;
    @Getter
    private String matchName;
    private String matchPassword;
    private boolean inProgress;
    private int maxPlayerCount;

    private int beatmapId;
    private String beatmapName;

    private int lowestRank;
    private int highestRank;

    private String winCondition;

    private List<MatchPlayerDto> players;


    private HBox leftContainer;
    private ImageView gamemodeImageView;
    private Label gamemodeLabel;
    private Label playerCountLabel;
    private Label rankRangeLabel;

    private HBox rightContainer;
    private MatchPlayerCard hostCard;
    private Label matchNameLabel;
    private Label beatmapNameLabel;
    private Map<Integer, MatchPlayerCard> playerCards; // map index to player

    public MatchCard(Integer matchId, String matchName, String matchPassword, boolean inProgress,
                        int maxPlayerCount, int beatmapId, String beatmapName,
                        int lowestRank, int highestRank, String winCondition,
                        List<MatchPlayerDto> players) {
        this.matchId = matchId;
        this.matchName = matchName;
        this.matchPassword = matchPassword;
        this.inProgress = inProgress;
        this.maxPlayerCount = maxPlayerCount;
        this.beatmapId = beatmapId;
        this.beatmapName = beatmapName;
        this.lowestRank = lowestRank;
        this.highestRank = highestRank;
        this.winCondition = winCondition;
        this.players = players != null ? new ArrayList<>(players) : new ArrayList<>();

        initializeComponents();
        setupLayout();
        setupStyling();
    }

    private void initializeComponents() {
        this.getStyleClass().add("match-card");

        leftContainer = new HBox();
        leftContainer.getStyleClass().add("left-container");

        gamemodeImageView = new ImageView();
        gamemodeImageView.getStyleClass().add("gamemode-icon");
        setGamemodeIcon();

        gamemodeLabel = new Label("osu! (head-to-head)");
        gamemodeLabel.getStyleClass().add("gamemode-label");

        playerCountLabel = new Label();
        playerCountLabel.setText(players.size() + " / " + maxPlayerCount);
        playerCountLabel.getStyleClass().add("player-count-label");

        rankRangeLabel = new Label();
        rankRangeLabel.setText("rank: " + formatRank(lowestRank) + " - " + formatRank(highestRank));
        rankRangeLabel.getStyleClass().add("rank-range-label");

        rightContainer = new HBox();
        rightContainer.getStyleClass().add("right-container");

        initializeHostCard();

        matchNameLabel = new Label();
        matchNameLabel.setText(matchName);
        matchNameLabel.getStyleClass().add("match-name-label");

        beatmapNameLabel = new Label();
        beatmapNameLabel.setText(beatmapName);
        beatmapNameLabel.getStyleClass().add("beatmap-name-label");

        playerCards = new HashMap<>();

        for (int i = 0; i < maxPlayerCount; i++) {
            MatchPlayerCard emptyCard = new MatchPlayerCard(
                    0, matchId, null, null, 0, null, null, false);
            playerCards.put(i, emptyCard);
        }

        for (MatchPlayerDto player : players) {
            if (player == null) {
                continue;
            }
            
            if (player.getRole() == PlayerRole.HOST) {
                MatchPlayerCard emptyCard = new MatchPlayerCard(
                        0, matchId, null, null, 0, null, null, false);
                playerCards.put(player.getMatchSlotIndex(), emptyCard);
                continue;
            }

            MatchPlayerCard playerCard = new MatchPlayerCard(
                    player.getId(), player.getMatchId(), player.getUserId(),
                    player.getUser().getUsername(), player.getUser().getRank(),
                    LocaleManager.getCountryName(player.getUser().getCountryCode()),
                    player.getUser().getProfilePicture(), false);
            playerCards.put(player.getMatchSlotIndex(), playerCard);
        }
    }

    private void initializeHostCard() {
        MatchPlayerDto host = null;
        for (MatchPlayerDto player : players) {
            if (player.getRole() == PlayerRole.HOST) {
                host = player;
                break;
            }
        }
        
        if (host != null) {
            String countryName = LocaleManager.getCountryName(host.getUser().getCountryCode());
            hostCard = new MatchPlayerCard(
                    host.getId(), host.getMatchId(), host.getUserId(), host.getUser().getUsername(),
                    host.getUser().getRank(), countryName, host.getUser().getProfilePicture(), true
            );
        } else {
            hostCard = new MatchPlayerCard(
                    0, matchId, null, null, 0, null, null, true);
        }
    }

    private void setupLayout() {
        HBox gameData = new HBox(playerCountLabel, rankRangeLabel);
        gameData.getStyleClass().add("game-data");

        VBox gameInfo = new VBox(gamemodeLabel, gameData);
        gameInfo.getStyleClass().add("game-info");

        leftContainer.getChildren().addAll(gamemodeImageView, gameInfo);
        this.getChildren().add(leftContainer);

        HBox matchPlayerCardsContainer = new HBox();
        matchPlayerCardsContainer.getStyleClass().add("match-player-cards-container");

        for (int i = 0; i < maxPlayerCount; i++) {
            MatchPlayerCard playerCard = playerCards.get(i);
            
            if (isHostSlot(i)) {
                continue;
            }
            
            matchPlayerCardsContainer.getChildren().add(playerCard);
        }

        for (int i = maxPlayerCount; i < 16; i++) {
            VBox inactiveCard = new VBox();
            inactiveCard.getStyleClass().add("inactive-card");
            matchPlayerCardsContainer.getChildren().add(inactiveCard);
        }

        VBox matchData = new VBox(matchNameLabel, beatmapNameLabel, matchPlayerCardsContainer);
        matchData.getStyleClass().add("match-data");
        rightContainer.getChildren().addAll(hostCard, matchData);

        this.getChildren().add(rightContainer);
    }

    private void setupStyling() {
        URL cssUrl = CssManager.getLobbyCssURL("MatchCard.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void setGamemodeIcon() {
        try {
            Image gamemodeImage = new Image(Objects.requireNonNull(
                    Main.class.getResource("/assets/gamemode/osu-gamemode.png")).toExternalForm());
            gamemodeImageView.setImage(gamemodeImage);
            gamemodeImageView.setFitHeight(45);
            gamemodeImageView.setFitWidth(45);
            gamemodeImageView.setTranslateY(3);
        } catch (Exception e) {
            System.err.println("Could not load gamemode icon: " + e.getMessage());
            gamemodeImageView.setImage(null);
        }
    }

    private void updateHostCard() {
        MatchPlayerDto host = null;
        
        for (MatchPlayerDto player : players) {
            if (player.getRole() == PlayerRole.HOST) {
                host = player;
                break;
            }
        }
        
        if (host != null) {
            String countryName = LocaleManager.getCountryName(host.getUser().getCountryCode());

            MatchPlayerCard newHostCard = new MatchPlayerCard(
                    host.getId(), host.getMatchId(), host.getUserId(), host.getUser().getUsername(),
                    host.getUser().getRank(), countryName, host.getUser().getProfilePicture(), true
            );
            
            if (!rightContainer.getChildren().isEmpty()) {
                rightContainer.getChildren().set(0, newHostCard);
                hostCard = newHostCard;
            } else {
                rightContainer.getChildren().add(0, newHostCard);
                hostCard = newHostCard;
            }
        }
    }

    public int getPlayerCount() {
        return players != null ? players.size() : 0;
    }

    public boolean hasPassword() {
        return matchPassword != null && !matchPassword.trim().isEmpty();
    }

    public void updatePlayerCount(int newPlayerCount) {
        if (playerCountLabel != null) {
            playerCountLabel.setText(newPlayerCount + " / " + maxPlayerCount);
        }
    }

    public void addPlayer(MatchPlayerDto player) {
        if (players == null) {
            return;
        }
        
        players.add(player);

        if (player.getRole() == PlayerRole.HOST) {
            updateHostCard();
        } else {
            MatchPlayerCard playerCard = new MatchPlayerCard(
                player.getId(), player.getMatchId(), player.getUserId(),
                player.getUser().getUsername(), player.getUser().getRank(),
                LocaleManager.getCountryName(player.getUser().getCountryCode()),
                player.getUser().getProfilePicture(), false);
            
            playerCards.put(player.getMatchSlotIndex(), playerCard);
        }
        
        refreshPlayerCardsDisplay();
        updatePlayerCount(players.size());
    }

    public void removePlayer(int userId) {
        if (players == null) {
            return;
        }
        
        MatchPlayerDto playerToRemove = null;
        int slotIndexToRemove = -1;
        
        for (MatchPlayerDto player : players) {
            if (player.getUserId() == userId) {
                playerToRemove = player;
                slotIndexToRemove = player.getMatchSlotIndex();
                break;
            }
        }
        
        if (playerToRemove != null) {
            players.remove(playerToRemove);
            
            if (slotIndexToRemove >= 0 && slotIndexToRemove < maxPlayerCount) {
                MatchPlayerCard emptyCard = new MatchPlayerCard(
                        0, matchId, null, null, 0, null, null, false);
                playerCards.put(slotIndexToRemove, emptyCard);
            }
            
            refreshPlayerCardsDisplay();
            updatePlayerCount(players.size());
        }
    }

    private void refreshPlayerCardsDisplay() {
        VBox matchData = (VBox) rightContainer.getChildren().get(1);
        HBox matchPlayerCardsContainer = (HBox) matchData.getChildren().get(2);
        
        matchPlayerCardsContainer.getChildren().clear();
        
        for (int i = 0; i < maxPlayerCount; i++) {
            MatchPlayerCard playerCard = playerCards.get(i);
            
            if (isHostSlot(i)) {
                continue;
            }
            
            matchPlayerCardsContainer.getChildren().add(playerCard);
        }

        int occupiedSlots = 0;
        for (int i = 0; i < maxPlayerCount; i++) {
            if (!isHostSlot(i)) {
                occupiedSlots++;
            }
        }
        
        for (int i = occupiedSlots; i < 16; i++) {
            VBox inactiveCard = new VBox();
            inactiveCard.getStyleClass().add("inactive-card");
            matchPlayerCardsContainer.getChildren().add(inactiveCard);
        }
    }

    private String formatRank(int rank) {
        return String.format("%,d", rank);
    }

    private boolean isHostSlot(int slotIndex) {
        for (MatchPlayerDto player : players) {
            if (player.getMatchSlotIndex() == slotIndex && 
                player.getRole() == beat.osu.shared.enums.match.PlayerRole.HOST) {
                return true;
            }
        }
        return false;
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
        
        updateHostCard();
        
        if (previousHostUserId != newHostUserId) {
            MatchPlayerDto previousHost = null;
            for (MatchPlayerDto player : players) {
                if (player.getUserId() == previousHostUserId) {
                    previousHost = player;
                    break;
                }
            }
            
            if (previousHost != null) {
                MatchPlayerCard playerCard = new MatchPlayerCard(
                    previousHost.getId(), previousHost.getMatchId(), previousHost.getUserId(),
                    previousHost.getUser().getUsername(), previousHost.getUser().getRank(),
                    LocaleManager.getCountryName(previousHost.getUser().getCountryCode()),
                    previousHost.getUser().getProfilePicture(), false);
                
                playerCards.put(previousHost.getMatchSlotIndex(), playerCard);
            }
            
            int newHostSlotIndex = findPlayerSlotIndex(newHostUserId);
            if (newHostSlotIndex != -1) {
                MatchPlayerCard emptyCard = new MatchPlayerCard(
                        0, matchId, null, null, 0, null, null, false);
                playerCards.put(newHostSlotIndex, emptyCard);
            }
        }
        
        refreshPlayerCardsDisplay();
    }
    
    private int findPlayerSlotIndex(int userId) {
        for (MatchPlayerDto player : players) {
            if (player.getUserId() == userId) {
                return player.getMatchSlotIndex();
            }
        }
        return -1;
    }
    
    public void hostLeft(int previousHostUserId, int newHostUserId) {
        if (players == null) {
            return;
        }
        
        players.removeIf(player -> player.getUserId() == previousHostUserId);
        
        int newHostSlotIndex = -1;
        for (MatchPlayerDto player : players) {
            if (player.getUserId() == newHostUserId) {
                newHostSlotIndex = player.getMatchSlotIndex();
                player.setRole(PlayerRole.HOST);
            } else {
                player.setRole(PlayerRole.PLAYER);
            }
        }
        
        if (newHostSlotIndex != -1) {
            MatchPlayerCard emptyCard = new MatchPlayerCard(
                    0, matchId, null, null, 0, null, null, false);
            playerCards.put(newHostSlotIndex, emptyCard);
        }
        
        updateHostCard();
        refreshPlayerCardsDisplay();
        updatePlayerCount(players.size());
    }

    public void movePlayerToSlot(int userId, int oldSlotIndex, int newSlotIndex) {
        if (players == null) {
            return;
        }
        
        MatchPlayerDto playerToMove = null;
        for (MatchPlayerDto player : players) {
            if (player.getUserId() == userId) {
                playerToMove = player;
                break;
            }
        }
        
        if (playerToMove == null) {
            System.err.println("Player with ID " + userId + " not found for slot move in match card");
            return;
        }
        
        playerToMove.setMatchSlotIndex(newSlotIndex);
        
        if (playerToMove.getRole() == PlayerRole.HOST) {
            return;
        }
        
        MatchPlayerCard emptyCard = new MatchPlayerCard(
                0, matchId, null, null, 0, null, null, false);
        playerCards.put(oldSlotIndex, emptyCard);
        
        boolean isNewSlotHost = isHostSlot(newSlotIndex);
        if (!isNewSlotHost) {
            MatchPlayerCard playerCard = new MatchPlayerCard(
                playerToMove.getId(), playerToMove.getMatchId(), playerToMove.getUserId(),
                playerToMove.getUser().getUsername(), playerToMove.getUser().getRank(),
                LocaleManager.getCountryName(playerToMove.getUser().getCountryCode()),
                playerToMove.getUser().getProfilePicture(), false);
            playerCards.put(newSlotIndex, playerCard);
        } else {
            MatchPlayerCard emptyHostSlotCard = new MatchPlayerCard(
                    0, matchId, null, null, 0, null, null, false);
            playerCards.put(newSlotIndex, emptyHostSlotCard);
        }
        
        refreshPlayerCardsDisplay();
        refreshPlayerCardsDisplay();
    }
}
