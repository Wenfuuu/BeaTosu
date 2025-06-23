package beat.osu.client.view.lobby.component.panels;

import beat.osu.client.helper.CssManager;
import beat.osu.client.view.lobby.component.ui.MatchFilters;
import beat.osu.shared.dto.match.MatchDto;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
}
