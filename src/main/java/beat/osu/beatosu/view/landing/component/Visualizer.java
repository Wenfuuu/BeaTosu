package beat.osu.beatosu.view.landing.component;

import beat.osu.beatosu.Main;
import beat.osu.beatosu.helper.CssManager;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.Objects;

public class Visualizer extends StackPane {

    private StackPane logoContainer;
    private ImageView logoView;
    private VBox menuBox;
    private MediaPlayer mediaPlayer;

    public Visualizer() {
        super();
        this.getStyleClass().add("visualizer");
        this.setAlignment(Pos.CENTER);

        // Initialize components
        initializeComponents();

        // Set layout
        setupLayout();

        // Load CSS
        loadStyles();
    }

    private void initializeComponents() {
        logoContainer = new StackPane();
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.getStyleClass().add("logo-container");

        logoView = new ImageView();
        try {
            logoView.setImage(new Image(Objects.requireNonNull(Main.class
                    .getResource("/assets/logo/osu_logo.png")).toExternalForm()));
        } catch (Exception e) {
            System.err.println("Error loading logo image: " + e.getMessage());
        }

        logoView.setFitWidth(450);
        logoView.setFitHeight(450);
        logoView.setPreserveRatio(true);

        // Apply glow effect
        DropShadow glow = new DropShadow();
        glow.setColor(Color.PINK);
        glow.setRadius(30);
        glow.setSpread(0.5);
        logoView.setEffect(glow);
    }

    private void setupLayout() {
        logoContainer.getChildren().add(logoView);
        this.getChildren().add(logoContainer);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getCssURL("Visualizer.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
    }

    // Set the menu box
    public void setMenuBox(VBox menuBox) {
        this.menuBox = menuBox;
        logoContainer.getChildren().clear();
        logoContainer.getChildren().add(menuBox);
        logoContainer.getChildren().add(logoView);
//        if (logoContainer.getChildren().size() > 1) {
//            logoContainer.getChildren().set(1, menuBox);
//        } else {
//            logoContainer.getChildren().add(menuBox);
//        }
    }

    // Get logo view for animation effects
    public ImageView getLogoView() {
        return logoView;
    }

    // Get logo container for positioning
    public StackPane getLogoContainer() {
        return logoContainer;
    }

    // Set up animation with media player
    public void setupAudioVisualization(MediaPlayer player) {
        this.mediaPlayer = player;

        if (player != null) {
            // Set up audio spectrum analysis
            player.setAudioSpectrumInterval(0.05); // Update 20 times per second
            player.setAudioSpectrumNumBands(16);   // Number of frequency bands
            player.setAudioSpectrumThreshold(-90); // Threshold in dB

            // Create audio spectrum listener for beat detection
            player.setAudioSpectrumListener((timestamp, duration, magnitudes, phases) -> {
                // Calculate average magnitude across bass frequencies (first few bands)
                double bassAvg = 0;
                int bassBands = 4; // Use first 4 bands for bass detection

                for (int i = 0; i < bassBands; i++) {
                    // Convert from dB to linear scale and add to average
                    bassAvg += Math.pow(10, magnitudes[i] / 20);
                }
                bassAvg /= bassBands;

                double minScale = 1.0;
                double maxScale = 1.5;
                double scaleFactor;

                if(bassAvg > 0.03) {
                    scaleFactor = minScale + Math.min(bassAvg * 5, maxScale - minScale);
                } else {
                    scaleFactor = minScale + Math.min(bassAvg * 3, maxScale - minScale);
                }

                // apply scale
                logoView.setScaleX(scaleFactor);
                logoView.setScaleY(scaleFactor);

                // adjust glow based on the beat
                DropShadow glow = (DropShadow) logoView.getEffect();
                glow.setRadius(20 + bassAvg * 40); // Make glow stronger with bass
                glow.setSpread(0.5); // makes the glow look thicker
            });
        }
    }
}
