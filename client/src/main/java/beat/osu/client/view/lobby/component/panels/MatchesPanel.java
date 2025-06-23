package beat.osu.client.view.lobby.component.panels;

import beat.osu.client.view.lobby.component.layout.NavigationBar;
import beat.osu.client.view.lobby.component.ui.MatchFilters;
import beat.osu.shared.dto.match.MatchDto;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class MatchListPanel extends VBox {

    private MatchFilters matchFilters;
    private NavigationBar navigationBar;

    private final List<MatchDto> matches;
    private VBox matchesContainer;
    private ScrollPane matchesScrollPane;

    public MatchListPanel() {
        this.matchFilters = new MatchFilters();
        this.navigationBar = new NavigationBar();

        this.matches = new ArrayList<>();

        setSpacing(10);
        getChildren().addAll(navigationBar, matchFilters);
    }

}
