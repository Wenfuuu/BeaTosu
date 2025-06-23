package beat.osu.client.view.lobby.component.ui;

import beat.osu.client.helper.CssManager;
import beat.osu.shared.dto.beatmap.requests.InsertBeatmapRequest;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.net.URL;

public class MatchFilters extends HBox {

    @Getter
    private CheckBox ownedBeatmapsCheckBox;
    @Getter
    private CheckBox showFullCheckBox;
    @Getter
    private CheckBox showLockedCheckBox;
    @Getter
    private CheckBox showInProgressCheckBox;

    private Label searchLabel;
    @Getter
    private TextField searchTextField;

    public MatchFilters() {
        initializeComponents();
        setLayout();
        loadStyles();
    }

    private void initializeComponents() {
        this.getStyleClass().add("match-filters");

        ownedBeatmapsCheckBox = new CheckBox("Owned Beatmaps");
        ownedBeatmapsCheckBox.getStyleClass().add("checkbox");

        showFullCheckBox = new CheckBox("Show Full");
        showFullCheckBox.getStyleClass().add("checkbox");

        showLockedCheckBox = new CheckBox("Show Locked");
        showLockedCheckBox.getStyleClass().add("checkbox");

        showInProgressCheckBox = new CheckBox("Show In-Progress");
        showInProgressCheckBox.getStyleClass().add("checkbox");

        searchLabel = new Label("Search:");
        searchLabel.getStyleClass().add("search-label");

        searchTextField = new TextField();
        searchTextField.getStyleClass().add("search-field");
    }

    private void setLayout() {
        VBox leftContainer = new VBox(8);
        leftContainer.getStyleClass().add("left-container");

        VBox rightContainer = new VBox(8);
        rightContainer.getStyleClass().add("right-container");

        leftContainer.getChildren().addAll(ownedBeatmapsCheckBox, showLockedCheckBox);
        rightContainer.getChildren().addAll(showFullCheckBox, showInProgressCheckBox);
        HBox.setMargin(searchLabel, new Insets(0, 12, 4, 0));
        HBox.setMargin(searchTextField, new Insets(0, 0, 4, 0));

        this.getChildren().addAll(leftContainer, rightContainer, searchLabel, searchTextField);
    }

    private void loadStyles() {
        try {
            URL cssUrl = CssManager.getLobbyCssURL("MatchFilters.css");
            if (cssUrl != null) {
                this.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Could not load MatchFilters CSS: " + e.getMessage());
        }
    }
}
