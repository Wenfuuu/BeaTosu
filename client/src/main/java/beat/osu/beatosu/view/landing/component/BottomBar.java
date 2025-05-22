package beat.osu.beatosu.view.landing.component;

import beat.osu.beatosu.helper.CssManager;
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

    public BottomBar() {
        super(10); // Spacing between children
        this.getStyleClass().add("bottom-bar");
        this.setPadding(new Insets(5, 10, 5, 10));

        // Initialize components
        initializeComponents();

        // Set layout
        setupLayout();

        // Load CSS
        loadStyles();
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
    }

    private void setupLayout() {
        this.setMaxHeight(65);
        this.getChildren().addAll(ppyLbl, ppyBox);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getLandingCssURL("BottomBar.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }
}
