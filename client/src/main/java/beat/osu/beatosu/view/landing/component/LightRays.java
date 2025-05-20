package beat.osu.beatosu.view.landing.component;

import javafx.animation.AnimationTimer;
// import javafx.animation.KeyFrame; // No longer needed
// import javafx.animation.KeyValue; // No longer needed
// import javafx.animation.Timeline; // No longer needed
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
// import javafx.util.Duration; // No longer needed

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LightRays extends Group {

    private final int NUM_RAYS = 200; // Consider reducing from 300 if still too heavy
    private static final double MIN_LENGTH = 0;
    private static final double MAX_LENGTH = 100;
    private final double CENTER_RADIUS = 260;
    private final List<Rectangle> rays = new ArrayList<>();
    private final Random random = new Random(); // Shared random for ray creation

    private double currentIntensity = 0.0;
    private double smoothingFactor = 0.5;

    private final List<RayState> rayStates = new ArrayList<>();
    private AnimationTimer animationTimer;

    private static class RayState {
        Rectangle ray;
        double currentLength;
        double targetLength;
        long startTimeNanos;
        long durationNanos;
        // Each RayState gets its own Random for independent animation timing/targets
        Random random = new Random();

        RayState(Rectangle ray) {
            this.ray = ray;
            this.currentLength = ray.getWidth();
            setNewTarget();
        }

        void setNewTarget() {
            this.targetLength = MIN_LENGTH + random.nextDouble() * (MAX_LENGTH - MIN_LENGTH);
            this.startTimeNanos = System.nanoTime();
            this.durationNanos = (long) ((250 + random.nextDouble() * 750) * 1_000_000); // millis to nanos
        }

        void update(long now) {
            long elapsedNanos = now - startTimeNanos;
            double progress = Math.min(1.0, (double) elapsedNanos / durationNanos);

            double newWidth = currentLength + (targetLength - currentLength) * progress;
            ray.setWidth(newWidth);

            if (progress >= 1.0) {
                currentLength = targetLength;
                setNewTarget();
            }
        }
    }

    public LightRays() {
        super();
        this.setCache(true); // Cache the LightRays group
        this.setCacheHint(CacheHint.SPEED);
        createRays();
        startUnifiedAnimation();
    }

    private void createRays() {
        for (int i = 0; i < NUM_RAYS; i++) {
            double angle = 360.0 * i / NUM_RAYS;
            double initialLength = MIN_LENGTH + random.nextDouble() * (MAX_LENGTH - MIN_LENGTH); // Use shared random
            double thickness = 2 + random.nextDouble() * 4; // Use shared random

            Rectangle ray = new Rectangle(
                    CENTER_RADIUS,
                    -thickness / 2.0,
                    initialLength,
                    thickness
            );
            ray.setArcWidth(thickness);
            ray.setArcHeight(thickness);
            ray.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.1 + random.nextDouble() * 0.4)); // Use shared random

            ray.getTransforms().add(new Rotate(angle, 0, 0));

            rays.add(ray);
            getChildren().add(ray);
            rayStates.add(new RayState(ray));
        }
    }

    public void startUnifiedAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (RayState state : rayStates) {
                    state.update(now);
                }
            }
        };
        animationTimer.start();
    }

    public void stopAnimations() {
        if (animationTimer != null) {
            animationTimer.stop();
            // animationTimer = null; // Optional: allow GC if LightRays might be reused later
        }
    }

    public void pulseWithAudio(double intensity) {
        currentIntensity = currentIntensity + (intensity - currentIntensity) * smoothingFactor;
        double scaleFactor = 1.0 + Math.min(currentIntensity * 1.5, 0.5);
        for (Rectangle ray : rays) {
            ray.setScaleX(scaleFactor);
            // If you want symmetrical pulsing for rays, also scale Y
            // ray.setScaleY(scaleFactor);
        }
    }
}
