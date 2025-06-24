package beat.osu.client.view.lobby.component.panels;

import beat.osu.client.helper.CssManager;
import beat.osu.client.view.lobby.component.cards.MatchCard;
import beat.osu.client.view.lobby.component.ui.MatchFilters;
import beat.osu.shared.dto.match.MatchDto;
import beat.osu.shared.dto.match.MatchPlayerDto;
import beat.osu.shared.dto.user.UserDto;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MatchesPanel extends VBox {

    private MatchFilters matchFilters;

    private final List<MatchDto> matches;
    private VBox matchesContainer;
    private ScrollPane matchesScrollPane;

    public MatchesPanel() {
        this.matches = new ArrayList<>();

        initializeComponents();
        setLayout();
        loadStyles();
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

    List<MatchPlayerDto> generateMatchPlayers(int matchId) {
        List<MatchPlayerDto> players = new ArrayList<>();

        UserDto hostUser = new UserDto(
                matchId, "osuHost" + matchId, "host" + matchId + "@example.com", "JP", null,
                9400 + matchId, 96.0 + matchId % 3, 1200 + matchId, 50 + matchId, 240 + matchId, true
        );
        players.add(new MatchPlayerDto(matchId, matchId, matchId, hostUser, "host", "active", 0));

        for (int i = 1; i <= 15; i++) {
            int userId = matchId * 100 + i;
            UserDto user = new UserDto(
                    userId, "osuUser" + userId, "user" + userId + "@example.com", "US", null,
                    10000 + userId, 95.0 + (userId % 5), 1000 + userId, 40 + userId % 10, 100 + userId % 50, false
            );

            Random rand = new Random();
            if (rand.nextInt(10) % 3 == 0) {
                players.add(null);
            } else {
                players.add(new MatchPlayerDto(userId, matchId, i, user, "player", "active", i));
            }
        }

        return players;
    }

    private void setLayout() {
        this.getChildren().addAll(matchFilters, matchesScrollPane);

        Random rand = new Random();

        for (int matchId = 1; matchId <= 6; matchId++) {
            MatchCard matchCard = new MatchCard(
                    matchId,
                    "Dummy Match " + matchId,
                    String.valueOf(1000 + matchId),
                    "open",
                    rand.nextInt(10) + 4,
                    123000 + matchId,
                    "Dummy Beatmap " + matchId,
                    282000 + matchId,
                    1800000 + matchId,
                    "Score",
                    generateMatchPlayers(matchId)
            );
            matchesContainer.getChildren().add(matchCard);
        }
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
}
