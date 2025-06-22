package beat.osu.client.view.landing.component.modals;

import beat.osu.client.helper.CssManager;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;

import java.net.URL;

public class SettingsModal extends StackPane {

    private Slider bgmVolumeSlider;
    private Slider sfxVolumeSlider;

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

    }

    private void setupAnimations() {

    }

    private void handleComponentEvents() {

    }
}
