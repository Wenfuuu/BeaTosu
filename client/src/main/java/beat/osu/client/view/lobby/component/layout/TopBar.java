package beat.osu.client.view.lobby.component.layout;

import java.net.URL;

import beat.osu.client.helper.CssManager;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class TopBar extends HBox {

    private Label titleLabel;
    private Label matchCountLabel;

    public TopBar() {
        super();
        this.getStyleClass().add("top-bar");

        initializeComponents();
        setupLayout();
        loadStyles();
    }

    private void initializeComponents() {
        titleLabel = new Label("Multiplayer Lobby");
        titleLabel.getStyleClass().add("title-label");

        matchCountLabel = new Label("Showing 308 of 308 matches");
        matchCountLabel.getStyleClass().add("match-count-label");

        this.getStyleClass().add("top-bar");
    }

    private void setupLayout() {
        this.setAlignment(Pos.CENTER_LEFT);

        this.getChildren().addAll(titleLabel, matchCountLabel);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getLobbyCssURL("TopBar.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }
}
