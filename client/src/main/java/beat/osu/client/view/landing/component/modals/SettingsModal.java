package beat.osu.client.view.landing.component.modals;

import beat.osu.client.factory.ButtonFactory;
import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.view.landing.component.ui.LightRays;
import beat.osu.client.view.landing.component.ui.Visualizer;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;

public class SettingsModal extends StackPane {

    private VBox formContainer;
    private TranslateTransition slideIn;
    private TranslateTransition slideOut;
    private boolean isModalVisible = false;

    private Slider bgmVolumeSlider;
    private Slider sfxVolumeSlider;
    private Button backButton;
    private Label volumeLabel;

    public SettingsModal() {
        initialize();
        setupAnimations();
        handleComponentEvents();

        URL globalCssUrl = CssManager.getGlobalCssURL();
        if (globalCssUrl != null) {
            this.getStylesheets().add(globalCssUrl.toExternalForm());
        } else {
            System.err.println("Global css file not found!");
        }

        URL cssUrl = CssManager.getLandingCssURL("SettingsModal.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("SettingsModal.css file not found!");
        }

        this.setVisible(false);
        this.setManaged(false);
    }

    private void initialize() {
        this.getStyleClass().add("settings-modal-background");
        this.setMaxWidth(650);

        formContainer = new VBox(20);
        formContainer.getStyleClass().add("settings-form-container");
        formContainer.setMaxWidth(300);
        formContainer.setMaxHeight(Region.USE_PREF_SIZE);
        formContainer.setAlignment(Pos.TOP_LEFT);

        volumeLabel = new Label("VOLUME");
        volumeLabel.getStyleClass().add("settings-title");

        // BGM Volume Section
        Label bgmLabel = new Label("Background Music Volume");
        bgmLabel.getStyleClass().add("settings-label");
        bgmVolumeSlider = new Slider(0, 1, 0.2);
        bgmVolumeSlider.getStyleClass().add("settings-slider");
        bgmVolumeSlider.setShowTickLabels(true);
        bgmVolumeSlider.setShowTickMarks(true);
        bgmVolumeSlider.setMajorTickUnit(0.25);
        bgmVolumeSlider.setBlockIncrement(0.1);
        VBox bgmBox = new VBox(10, bgmLabel, bgmVolumeSlider);

        // SFX Volume Section
        Label sfxLabel = new Label("Sound Effects Volume");
        sfxLabel.getStyleClass().add("settings-label");
        sfxVolumeSlider = new Slider(0, 1, 0.2);
        sfxVolumeSlider.getStyleClass().add("settings-slider");
        sfxVolumeSlider.setShowTickLabels(true);
        sfxVolumeSlider.setShowTickMarks(true);
        sfxVolumeSlider.setMajorTickUnit(0.25);
        sfxVolumeSlider.setBlockIncrement(0.1);
        VBox sfxBox = new VBox(10, sfxLabel, sfxVolumeSlider);

        backButton = ButtonFactory.createBackButton();
        StackPane.setAlignment(backButton, Pos.BOTTOM_LEFT);
        StackPane.setMargin(backButton, new Insets(0, 0, 12, 0));

        formContainer.getChildren().addAll(
                volumeLabel,
                bgmBox,
                sfxBox);

        this.getChildren().addAll(formContainer, backButton);
        StackPane.setAlignment(formContainer, Pos.CENTER_RIGHT);
    }

    private void setupAnimations() {
        slideIn = new TranslateTransition(Duration.millis(150), this);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);
        slideIn.setOnFinished(e -> {
            isModalVisible = true;
            formContainer.setCacheHint(CacheHint.DEFAULT);

            LightRays rays = findLightRays();
            if (rays != null) {
                rays.startUnifiedAnimation();
            }
        });

        slideOut = new TranslateTransition(Duration.millis(150), this);
        slideOut.setInterpolator(Interpolator.EASE_IN);
        slideOut.setOnFinished(event -> {
            super.setVisible(false);
            super.setManaged(false);
            isModalVisible = false;

            LightRays rays = findLightRays();
            if (rays != null) {
                rays.startUnifiedAnimation();
            }
        });

        formContainer.setCache(true);
        formContainer.setCacheHint(CacheHint.SPEED);
    }

    private LightRays findLightRays() {
        if (getScene() == null || getScene().getRoot() == null) {
            return null;
        }

        Parent root = getScene().getRoot();

        for (Node node : root.getChildrenUnmodifiable()) {
            if (node instanceof StackPane || node instanceof BorderPane) {
                for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                    if (child instanceof Visualizer) {
                        return ((Visualizer) child).getLightRays();
                    }
                }
            }
        }

        return null;
    }

    private void handleComponentEvents() {
        bgmVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("BGM Volume changed to: " + newValue);
            BgmManager.getInstance().setBGM_VOLUME((Double) newValue);
            BgmManager.getInstance().getCurrentPlayer().setVolume((Double) newValue);
        });

        sfxVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("SFX Volume changed to: " + newValue);
            SfxManager.setSFX_VOLUME((Double) newValue);
        });

        backButton.setOnAction(e -> hide());
    }

    public void show() {
        if (isModalVisible || (slideIn != null && slideIn.getStatus() == Animation.Status.RUNNING)
                || (slideOut != null && slideOut.getStatus() == Animation.Status.RUNNING)) {
            return;
        }

        LightRays rays = findLightRays();
        if (rays != null) {
            rays.stopAnimations();
        }

        this.setCache(true);
        this.setCacheHint(CacheHint.SPEED);
        this.setTranslateX(-500);
        super.setManaged(true);
        super.setVisible(true);
//        this.toFront();

        slideIn.setFromX(this.getTranslateX());
        slideIn.play();
    }

    public void hide() {
        if (!isModalVisible || (slideOut != null && slideOut.getStatus() == Animation.Status.RUNNING)) {
            return;
        }

        isModalVisible = false;

        LightRays rays = findLightRays();
        if (rays != null) {
            rays.stopAnimations();
        }

        this.setCache(true);
        this.setCacheHint(CacheHint.SPEED);

        slideOut.setFromX(this.getTranslateX());
        slideOut.setToX(-500);
        slideOut.play();
    }

    public boolean isShowing() {
        return isModalVisible || (slideIn != null && slideIn.getStatus() == Animation.Status.RUNNING);
    }
}
