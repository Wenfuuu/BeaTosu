package beat.osu.client.view.lobby.component.ui;

import java.net.URL;

import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.SfxManager;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

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
        setupInputFieldSounds();
    }

    private void setupInputFieldSounds() {
        searchTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.BACK_SPACE) {
                SfxManager.playSfx("key-delete.mp3");
            } else {
                int randomKeyPress = (int) (Math.random() * 4) + 1;
                SfxManager.playSfx("key-press-" + randomKeyPress + ".mp3");
            }
        });
    }

    private void initializeComponents() {
        this.getStyleClass().add("match-filters");

        ownedBeatmapsCheckBox = new CheckBox("Owned Beatmaps");
        ownedBeatmapsCheckBox.getStyleClass().add("checkbox");
        ownedBeatmapsCheckBox.setSelected(true);

        showFullCheckBox = new CheckBox("Show Full");
        showFullCheckBox.getStyleClass().add("checkbox");
        showFullCheckBox.setSelected(true); 

        showLockedCheckBox = new CheckBox("Show Locked");
        showLockedCheckBox.getStyleClass().add("checkbox");
        showLockedCheckBox.setSelected(true);

        showInProgressCheckBox = new CheckBox("Show In-Progress");
        showInProgressCheckBox.getStyleClass().add("checkbox");
        showInProgressCheckBox.setSelected(true);

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
        HBox.setMargin(searchLabel, new Insets(0, 12, 6, 0));
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
