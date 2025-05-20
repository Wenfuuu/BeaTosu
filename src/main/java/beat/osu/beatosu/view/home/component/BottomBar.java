package beat.osu.beatosu.view.home.component;

import beat.osu.beatosu.Main;
import beat.osu.beatosu.helper.CssManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.net.URL;
import java.util.Objects;

public class BottomBar extends HBox {
    private Button backBtn;
    private ImageView logoView;
    private Region spacer;

    public BottomBar() {
        this.getStyleClass().add("bot-bar");

        initializeComponents();
        setupLayout();
        loadStyles();
    }

    private void initializeComponents() {
        backBtn = new Button(" < back");

        spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        logoView = new ImageView();
        logoView.setImage(new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/logo/osu_logo.png")).toExternalForm()));
        logoView.setFitWidth(80);
        logoView.setFitHeight(80);
        logoView.setTranslateX(-20);
        logoView.setScaleX(2);
        logoView.setScaleY(2);
        logoView.setPreserveRatio(true);
    }

    private void setupLayout() {
        this.getChildren().addAll(backBtn, spacer, logoView);
        this.setAlignment(Pos.CENTER);
        this.toFront();
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getHomeCssURL("BottomBar.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    public Button getBackButton() {
        return backBtn;
    }

    public ImageView getLogoView() {
        return logoView;
    }
}
