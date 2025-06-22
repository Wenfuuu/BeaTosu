package beat.osu.client.view.home.component;

import beat.osu.client.Main;
import beat.osu.client.factory.ButtonFactory;
import beat.osu.client.helper.CssManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import lombok.Getter;

import java.net.URL;
import java.util.Objects;

public class BottomBar extends HBox {
    @Getter
    private Button backButton;
    @Getter
    private ImageView logoView;
    private Region spacer;

    public BottomBar() {
        this.getStyleClass().add("bot-bar");

        initializeComponents();
        setupLayout();
        loadStyles();
    }

    private void initializeComponents() {
        backButton = ButtonFactory.createBackButton();

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
        this.getChildren().addAll(backButton, spacer, logoView);
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
}
