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

        updateHostCard();

        matchNameLabel = new Label();
        matchNameLabel.setText(matchName);
        matchNameLabel.getStyleClass().add("match-name-label");

        beatmapNameLabel = new Label();
        beatmapNameLabel.setText(beatmapName);
        beatmapNameLabel.getStyleClass().add("beatmap-name-label");

        playerCards = new HashMap<>();

        for (int i = 1; i < maxPlayerCount; i++) {
            MatchPlayerCard emptyCard = new MatchPlayerCard(
                    0, matchId, null, null, 0, null, null, false);
            playerCards.put(i, emptyCard);
        }

        for (int i = 1; i < players.size(); i++) {
            MatchPlayerDto player = players.get(i);
            if (player == null) {
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

    private void setupLayout() {
        HBox gameData = new HBox(playerCountLabel, rankRangeLabel);
        gameData.getStyleClass().add("game-data");

        VBox gameInfo = new VBox(gamemodeLabel, gameData);
        gameInfo.getStyleClass().add("game-info");

        leftContainer.getChildren().addAll(gamemodeImageView, gameInfo);
        this.getChildren().add(leftContainer);

        HBox matchPlayerCardsContainer = new HBox();
        matchPlayerCardsContainer.getStyleClass().add("match-player-cards-container");

        for (int i = 1; i < maxPlayerCount; i++) {
            MatchPlayerCard playerCard = playerCards.get(i);
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
        MatchPlayerDto host = players.get(0);
        String countryName = LocaleManager.getCountryName(host.getUser().getCountryCode());

        hostCard = new MatchPlayerCard(
                host.getId(), host.getMatchId(), host.getUserId(), host.getUser().getUsername(),
                host.getUser().getRank(), countryName, host.getUser().getProfilePicture(), true
        );
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

        for (int i = 1; i < maxPlayerCount; i++) {
            MatchPlayerCard existingCard = playerCards.get(i);
            if (existingCard != null && existingCard.getUserId() == null) {
                MatchPlayerCard playerCard = new MatchPlayerCard(
                    player.getId(), player.getMatchId(), player.getUserId(),
                    player.getUser().getUsername(), player.getUser().getRank(),
                    LocaleManager.getCountryName(player.getUser().getCountryCode()),
                    player.getUser().getProfilePicture(), false);
                playerCards.put(player.getMatchSlotIndex(), playerCard);
                
                refreshPlayerCardsDisplay();
                break;
            }
        }
        
        updatePlayerCount(players.size());
    }

    private void refreshPlayerCardsDisplay() {
        VBox matchData = (VBox) rightContainer.getChildren().get(1);
        HBox matchPlayerCardsContainer = (HBox) matchData.getChildren().get(2);
        
        matchPlayerCardsContainer.getChildren().clear();
        
        for (int i = 1; i < maxPlayerCount; i++) {
            MatchPlayerCard playerCard = playerCards.get(i);
            matchPlayerCardsContainer.getChildren().add(playerCard);
        }

        for (int i = maxPlayerCount; i < 16; i++) {
            VBox inactiveCard = new VBox();
            inactiveCard.getStyleClass().add("inactive-card");
            matchPlayerCardsContainer.getChildren().add(inactiveCard);
        }
    }

    private String formatRank(int rank) {
        return String.format("%,d", rank);
    }
}
