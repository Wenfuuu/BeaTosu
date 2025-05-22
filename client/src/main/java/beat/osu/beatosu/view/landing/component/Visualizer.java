package beat.osu.beatosu.view.landing.component;

import beat.osu.beatosu.Main;
import beat.osu.beatosu.helper.CssManager;
import javafx.scene.CacheHint;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import lombok.Getter;

import java.net.URL;
import java.util.Objects;

public class Visualizer extends StackPane {

    @Getter
    private StackPane logoContainer;
    @Getter
    private ImageView logoView;
    private VBox menuBox;
    @Getter
    private LightRays lightRays;
    @Getter
    private StackPane logoRayGroup;

    private double visualizerSize;
    private double currentScaleFactor = 1.0;
    private double currentGlowRadius = 20.0;
    private double smoothingFactor = 0.5;

    private boolean audioVisualizationPaused = false;

    public Visualizer() {
        super();
        this.getStyleClass().add("visualizer");
        this.setAlignment(Pos.CENTER);

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double screenSize = Math.min(visualBounds.getWidth(), visualBounds.getHeight());
        this.visualizerSize = screenSize * 0.70;

        initializeComponents();
        setupLayout();
        loadStyles();
    }

    private void initializeComponents() {
        lightRays = new LightRays(visualizerSize / 2.0); // Use double visualizerSize

        logoRayGroup = new StackPane();
        logoRayGroup.setAlignment(Pos.CENTER);
        logoRayGroup.setCache(true); // Cache the group that gets translated
        logoRayGroup.setCacheHint(CacheHint.SPEED);

        logoContainer = new StackPane();
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.getStyleClass().add("logo-container");
        // Caching logoContainer might be redundant if logoRayGroup is cached,
        // but harmless. If logoView is its only complex child, caching logoView is key.

        logoView = new ImageView();
        try {
            logoView.setImage(new Image(Objects.requireNonNull(Main.class
                    .getResource("/assets/logo/osu_logo.png")).toExternalForm()));
        } catch (Exception e) {
            System.err.println("Error loading logo image: " + e.getMessage());
            // Consider a placeholder or default behavior
        }

        logoView.setFitWidth(visualizerSize);
        logoView.setFitHeight(visualizerSize);
        logoView.setPreserveRatio(true);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.PINK);
        glow.setRadius(30);
        glow.setSpread(0.5); // Consider reducing spread if still heavy (e.g., 0.2 or 0)
        logoView.setEffect(glow);

        logoView.setCache(true); // Crucial for animated effects + transforms
        logoView.setCacheHint(CacheHint.SPEED);
    }

    private void setupLayout() {
        logoRayGroup.getChildren().add(lightRays);
        logoContainer.getChildren().add(logoView); // logoView added first
        logoRayGroup.getChildren().add(logoContainer);
        this.getChildren().add(logoRayGroup);
    }

    private void loadStyles() {
        URL cssUrl = CssManager.getLandingCssURL("Visualizer.css");
        if (cssUrl != null) {
            this.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("CSS file for Visualizer not found!");
        }
    }

    public void setMenuBox(VBox menuBox) {
        this.menuBox = menuBox;
        // Ensure logoView remains on top of menuBox if they are siblings in logoContainer
        // Or, more simply, add menuBox to logoRayGroup BEHIND logoContainer
        // Current: menuBox added to logoContainer, then logoView. This means logoView is on top.
        if (logoContainer.getChildren().contains(this.menuBox)) {
            logoContainer.getChildren().remove(this.menuBox);
        }
        if (this.menuBox != null) {
            logoContainer.getChildren().add(0, this.menuBox); // Add menuBox behind logoView
        }
    }

    public void setupAudioVisualization(MediaPlayer player) {
        if (player == null) return;

        player.setAudioSpectrumInterval(0.1);
        player.setAudioSpectrumNumBands(16);
        player.setAudioSpectrumThreshold(-90);

        player.setAudioSpectrumListener((timestamp, duration, magnitudes, phases) -> {
            if (audioVisualizationPaused) {
                // If paused, we might want to slowly return to a base state
                // rather than freezing abruptly. For now, just skip updates.
                return;
            }

            double bassAvg = 0;
            int bassBands = 4;
            for (int i = 0; i < bassBands; i++) {
                bassAvg += Math.pow(10, magnitudes[i] / 20);
            }
            bassAvg /= bassBands;

            double minScale = 1.0;
            double maxScale = 1.15;
            double targetScaleFactor = minScale + Math.min(bassAvg * (bassAvg > 0.03 ? 1.75 : 1.4), maxScale - minScale);
            double targetGlowRadius = 20 + bassAvg * 30;

            currentScaleFactor += (targetScaleFactor - currentScaleFactor) * smoothingFactor;
            currentGlowRadius += (targetGlowRadius - currentGlowRadius) * smoothingFactor;

            logoView.setScaleX(currentScaleFactor);
            logoView.setScaleY(currentScaleFactor);

            if (logoView.getEffect() instanceof DropShadow) {
                DropShadow glow = (DropShadow) logoView.getEffect();
                glow.setRadius(currentGlowRadius);
            }

            if (lightRays != null) {
                lightRays.pulseWithAudio(currentScaleFactor - 1.0);
            }
        });
    }
}
