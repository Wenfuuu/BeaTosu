package beat.osu.client.view.landing.component.layout;

import java.net.URL;

import beat.osu.client.helper.CssManager;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class BottomBar extends HBox {

    private Label ppyLbl;
    private Label copyright;
    private Label website;
    private VBox ppyBox;

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
        }
    }

    public void setFullOpacity() {
        FadeTransition fadeToFull = new FadeTransition(Duration.millis(300), this);
        fadeToFull.setFromValue(this.getOpacity());
        fadeToFull.setToValue(1.0);
        fadeToFull.play();
    }

    public void setLowOpacity() {
        FadeTransition fadeToLow = new FadeTransition(Duration.millis(300), this);
        fadeToLow.setFromValue(this.getOpacity());
        fadeToLow.setToValue(0.2);
        fadeToLow.play();
    }
}
