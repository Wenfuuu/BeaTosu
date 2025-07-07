package beat.osu.client.view.landing.component.ui;

import beat.osu.client.Main;
import beat.osu.client.events.song.SongChangeEvent;
import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.CssManager;
import beat.osu.client.interfaces.song.SongEventListener;
import javafx.scene.CacheHint;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import lombok.Getter;

import java.net.URL;
import java.util.Objects;

public class Visualizer extends StackPane implements SongEventListener {

    @Getter
    private StackPane logoContainer;
    @Getter
    private ImageView logoView;
    private VBox menuBox;
    private VBox subMenuBox;
    @Getter
    private LightRays lightRays;
    @Getter
    private StackPane logoRayGroup;

    private double visualizerSize;
    private double currentScaleFactor = 1.0;
    private double currentGlowRadius = 20.0;
    private double smoothingFactor = 0.5;

    private boolean audioVisualizationPaused = false;

    public Visualizer(double visualizerSize) {
        super();
        this.getStyleClass().add("visualizer");
        this.setAlignment(Pos.CENTER);
        this.visualizerSize = visualizerSize;

        initializeComponents();
        setupLayout();
        loadStyles();
    }

    private void initializeComponents() {
        lightRays = new LightRays(visualizerSize / 2.0 - 10);

        logoRayGroup = new StackPane();
        logoRayGroup.setAlignment(Pos.CENTER);
        logoRayGroup.setCache(true);
        logoRayGroup.setCacheHint(CacheHint.SPEED);

        logoContainer = new StackPane();
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.getStyleClass().add("logo-container");

        logoView = new ImageView();
        try {
            logoView.setImage(new Image(Objects.requireNonNull(Main.class
                    .getResource("/assets/images/misc/osu_logo.png")).toExternalForm()));
        } catch (Exception e) {
            System.err.println("Error loading logo image: " + e.getMessage());
        }

        logoView.setFitWidth(visualizerSize);
        logoView.setFitHeight(visualizerSize);
        logoView.setPreserveRatio(true);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.PINK);
        glow.setRadius(30);
        glow.setSpread(0.5);
        logoView.setEffect(glow);

        logoView.setCache(true);
        logoView.setCacheHint(CacheHint.SPEED);
    }

    private void setupLayout() {
        logoRayGroup.getChildren().add(lightRays);
        logoContainer.getChildren().add(logoView);
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
        if (logoContainer.getChildren().contains(this.menuBox)) {
            logoContainer.getChildren().remove(this.menuBox);
        }
        if (this.menuBox != null) {
            logoContainer.getChildren().add(0, this.menuBox);
        }
    }

    public void setSubMenuBox(VBox subMenuBox) {
        this.subMenuBox = subMenuBox;
        if (logoContainer.getChildren().contains(this.subMenuBox)) {
            logoContainer.getChildren().remove(this.subMenuBox);
        }
        if (this.subMenuBox != null) {
            logoContainer.getChildren().add(0, this.subMenuBox);
        }
    }

    public void setupAudioVisualization(MediaPlayer player) {
        if (player == null) return;

        player.setAudioSpectrumInterval(0.1);
        player.setAudioSpectrumNumBands(16);
        player.setAudioSpectrumThreshold(-90);

        player.setAudioSpectrumListener((timestamp, duration, magnitudes, phases) -> {
            if (audioVisualizationPaused) {
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

    @Override
    public void update(SongChangeEvent event) {
        setupAudioVisualization(BgmManager.getInstance().getCurrentPlayer());
    }
}
