package beat.osu.client.view.landing.component.modals;

import beat.osu.client.config.ConfigurationManager;
import beat.osu.client.factory.ButtonFactory;
import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.helper.InputManager;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.view.landing.component.ui.LightRays;
import beat.osu.client.view.landing.component.ui.Visualizer;
import beat.osu.client.view.shared.common.Toast;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
    private Button backButton;

    // General components
    private Label gameplayLabel;
    private Slider backgroundDimSlider;

    // Keybind components
    private Label keybindLabel;
    private Button leftClickKeybind;
    private Button rightClickKeybind;
    private Button currentKeybindButton; // Track which button is being configured

    // Volume components
    private Label volumeLabel;
    private Slider bgmVolumeSlider;
    private Slider sfxVolumeSlider;
    private CheckBox ignoreBeatmapHitsoundsCheckBox;

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
        formContainer.setMaxWidth(200);
        formContainer.setMaxHeight(Region.USE_PREF_SIZE);
        formContainer.setAlignment(Pos.TOP_LEFT);

        gameplayLabel = new Label("GAMEPLAY");
        gameplayLabel.getStyleClass().add("settings-title");

        Label backgroundDimLabel = new Label("Background Dim");
        backgroundDimLabel.getStyleClass().add("settings-label");
        backgroundDimSlider = new Slider(0.25, 1, ConfigurationManager.getInstance().getBackgroundDim());
        backgroundDimSlider.getStyleClass().add("settings-slider");
        backgroundDimSlider.setMajorTickUnit(0.25);
        backgroundDimSlider.setBlockIncrement(0.1);
        VBox generalBox = new VBox(10, backgroundDimLabel, backgroundDimSlider);

        // Keybind Settings Section
        keybindLabel = new Label("KEYBINDS");
        keybindLabel.getStyleClass().add("settings-title");

        // Left Click Keybind
        Label leftClickLabel = new Label("Left Click");
        leftClickLabel.getStyleClass().add("settings-label");
        leftClickKeybind = new Button(InputManager.getKeybind1().name());
        leftClickKeybind.getStyleClass().add("keybind-button");
        leftClickKeybind.setMaxWidth(Double.MAX_VALUE);
        VBox leftClickBox = new VBox(10, leftClickLabel, leftClickKeybind);

        // Right Click Keybind
        Label rightClickLabel = new Label("Right Click");
        rightClickLabel.getStyleClass().add("settings-label");
        rightClickKeybind = new Button(InputManager.getKeybind2().name());
        rightClickKeybind.getStyleClass().add("keybind-button");
        rightClickKeybind.setMaxWidth(Double.MAX_VALUE);
        VBox rightClickBox = new VBox(10, rightClickLabel, rightClickKeybind);

        volumeLabel = new Label("VOLUME");
        volumeLabel.getStyleClass().add("settings-title");

        // BGM Volume Section
        Label bgmLabel = new Label("Background Music Volume");
        bgmLabel.getStyleClass().add("settings-label");
        bgmVolumeSlider = new Slider(0, 1, BgmManager.getInstance().getBGM_VOLUME());
        bgmVolumeSlider.getStyleClass().add("settings-slider");
        // bgmVolumeSlider.setShowTickLabels(true);
        // bgmVolumeSlider.setShowTickMarks(true);
        bgmVolumeSlider.setMajorTickUnit(0.25);
        bgmVolumeSlider.setBlockIncrement(0.1);
        VBox bgmBox = new VBox(10, bgmLabel, bgmVolumeSlider);

        // SFX Volume Section
        Label sfxLabel = new Label("Sound Effects Volume");
        sfxLabel.getStyleClass().add("settings-label");
        sfxVolumeSlider = new Slider(0, 1, SfxManager.getSFX_VOLUME());
        sfxVolumeSlider.getStyleClass().add("settings-slider");
        // sfxVolumeSlider.setShowTickLabels(true);
        // sfxVolumeSlider.setShowTickMarks(true);
        sfxVolumeSlider.setMajorTickUnit(0.25);
        sfxVolumeSlider.setBlockIncrement(0.1);
        VBox sfxBox = new VBox(10, sfxLabel, sfxVolumeSlider);

        // Ignore Beatmap Hitsounds Checkbox
        ignoreBeatmapHitsoundsCheckBox = new CheckBox("Ignore beatmap hitsounds");
        ignoreBeatmapHitsoundsCheckBox.getStyleClass().add("settings-checkbox");
        ignoreBeatmapHitsoundsCheckBox.setSelected(SfxManager.isIgnoreBeatmapSFX());

        backButton = ButtonFactory.createBackButton();
        StackPane.setAlignment(backButton, Pos.BOTTOM_LEFT);
        StackPane.setMargin(backButton, new Insets(0, 0, 12, 0));
        formContainer.getChildren().addAll(
                keybindLabel,
                leftClickBox,
                rightClickBox,
                volumeLabel,
                bgmBox,
                sfxBox,
                ignoreBeatmapHitsoundsCheckBox,
                gameplayLabel,
                generalBox);

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

    private void resetKeybindButton(Button button) {
        button.getStyleClass().remove("keybind-button-waiting");
        // Reset to previous keybind value
        if (button == leftClickKeybind) {
            button.setText(InputManager.getKeybind1().getName().toUpperCase()); // Default or get from InputManager
        } else if (button == rightClickKeybind) {
            button.setText(InputManager.getKeybind2().getName().toUpperCase()); // Default or get from InputManager
        }
        currentKeybindButton = null;
    }

    private void handleComponentEvents() {
        // Keybind configuration handlers
        leftClickKeybind.setOnAction(e -> {
            // Reset other keybind button if it was focused
            resetKeybindButton(rightClickKeybind);

            currentKeybindButton = leftClickKeybind;
            leftClickKeybind.setText("Press a key...");
            leftClickKeybind.getStyleClass().add("keybind-button-waiting");
            this.requestFocus();
        });

        rightClickKeybind.setOnAction(e -> {
            // Reset other keybind button if it was focused
            resetKeybindButton(leftClickKeybind);

            currentKeybindButton = rightClickKeybind;
            rightClickKeybind.setText("Press a key...");
            rightClickKeybind.getStyleClass().add("keybind-button-waiting");
            this.requestFocus();
        });

        this.setOnKeyPressed(event -> {
            System.out.println("Key pressed: " + event.getCode());
            if (currentKeybindButton != null) {
                currentKeybindButton.getStyleClass().remove("keybind-button-waiting");
                if (!event.getCode().isLetterKey()) {
                    resetKeybindButton(currentKeybindButton);
                    System.out.println("Only letters are allowed for keybinds");
                    Toast.error("Only letters are allowed for keybinds").show();
                    return;
                }

                if (currentKeybindButton == leftClickKeybind) {
                    if (event.getCode() == InputManager.getKeybind2()) {
                        resetKeybindButton(currentKeybindButton);
                        System.out.println("This key is already bound to another action");
                        Toast.error("This key is already bound to another action").show();
                        return;
                    }
                    InputManager.setKeybind1(event.getCode());
                } else if (currentKeybindButton == rightClickKeybind) {
                    if (event.getCode() == InputManager.getKeybind1()) {
                        resetKeybindButton(currentKeybindButton);
                        System.out.println("This key is already bound to another action");
                        Toast.error("This key is already bound to another action").show();
                        return;
                    }
                    InputManager.setKeybind2(event.getCode());
                }

                String keyName = event.getCode().getName().toUpperCase();
                currentKeybindButton.setText(keyName);
                currentKeybindButton = null;
                event.consume();
            }
        });

        // Volume slider handlers
        bgmVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            BgmManager.getInstance().setBGM_VOLUME((Double) newValue);
            BgmManager.getInstance().getCurrentPlayer().setVolume((Double) newValue);
        });

        sfxVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            SfxManager.setSFX_VOLUME((Double) newValue);
        });

        backgroundDimSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ConfigurationManager.getInstance().setBackgroundDim((Double) newValue);
        });

        // Checkbox event handler
        ignoreBeatmapHitsoundsCheckBox.setOnAction(e -> {
            boolean ignoreHitsounds = ignoreBeatmapHitsoundsCheckBox.isSelected();
            SfxManager.setIgnoreBeatmapSFX(ignoreHitsounds);
            System.out.println("Ignore beatmap hitsounds: " + ignoreHitsounds);
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
        // this.toFront();

        slideIn.setFromX(this.getTranslateX());
        slideIn.play();
    }

    public void hide() {
        if (!isModalVisible || (slideOut != null && slideOut.getStatus() == Animation.Status.RUNNING)) {
            return;
        }

        resetKeybindButton(leftClickKeybind);
        resetKeybindButton(rightClickKeybind);
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
