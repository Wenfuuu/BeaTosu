package beat.osu.client.view.landing.component.layout;

import beat.osu.client.controller.ConnectedUsersController;
import beat.osu.client.helper.CssManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;

public class BottomBar extends HBox {

    private Label ppyLbl;
    private Label copyright;
    private Label website;
    private VBox ppyBox;

    private Label userCountLabel;
    private ConnectedUsersController connectedUsersController;

    public BottomBar() {
        super(10);
        this.getStyleClass().add("bottom-bar");
        this.setPadding(new Insets(5, 10, 5, 10));

        // Initialize components
        initializeComponents();

        // Set layout
        setupLayout();

        // Load CSS
        loadStyles();

        setupUserCountSubscription();
    }

    private void initializeComponents() {
        ppyLbl = new Label("ppy");
        ppyLbl.getStyleClass().add("ppy-label");

        copyright = new Label("ppy powered 2007-2025");
        copyright.getStyleClass().add("copyright");

        website = new Label("osu.ppy.sh");
        website.getStyleClass().add("website");

        ppyBox = new VBox(copyright, website);
        ppyBox.setAlignment(Pos.CENTER_LEFT);

        connectedUsersController = new ConnectedUsersController();
        userCountLabel = new Label("Users online: N/A");
        userCountLabel.setAlignment(Pos.CENTER_RIGHT);
    }

    private void setupLayout() {
        this.setMaxHeight(65);
        this.getChildren().addAll(ppyLbl, ppyBox, userCountLabel);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getLandingCssURL("BottomBar.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    private void setupUserCountSubscription() {
        connectedUsersController.addUserCountCallback(this::updateUserCountLabel);
    }

    private void updateUserCountLabel(Integer userCount) {
        Platform.runLater(() -> {
            if (userCount != null) {
                userCountLabel.setText("Users online: " + userCount);
            } else {
                userCountLabel.setText("Users online: N/A");
            }
        });
    }

    public void cleanup() {
        if (connectedUsersController != null) {
            connectedUsersController.removeUserCountCallback(this::updateUserCountLabel);
        }
    }
}
