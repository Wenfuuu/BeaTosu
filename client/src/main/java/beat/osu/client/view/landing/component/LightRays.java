package beat.osu.client.view.landing.component;

import javafx.animation.AnimationTimer;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;

import java.util.*;

public class LightRays extends Group {

    private final int NUM_RAYS = 200;
    private static final double MIN_LENGTH = 0;
    private static final double MAX_LENGTH = 125;
    private final List<Rectangle> rays = new ArrayList<>();
    private final Random random = new Random();

    private double centerRadius;
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
        Random random = new Random();

        RayState(Rectangle ray) {
            this.ray = ray;
            this.currentLength = ray.getWidth();
            setNewTarget();
        }

        void setNewTarget() {
            this.targetLength = MIN_LENGTH + random.nextDouble() * (MAX_LENGTH - MIN_LENGTH);
            this.startTimeNanos = System.nanoTime();
            this.durationNanos = (long) ((250 + random.nextDouble() * 750) * 1_000_000);
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

    private void optimizeSceneGraph() {
        // Clear existing children
        getChildren().clear();

        // Create groups for rays by angle segments (for example, group by 45-degree segments)
        Map<Integer, Group> rayGroups = new HashMap<>();

        for (int i = 0; i < rays.size(); i++) {
            Rectangle ray = rays.get(i);
            // Group rays by angle segment (0-44, 45-89, etc.)
            int angleSegment = (i * 360 / NUM_RAYS) / 45;

            Group group = rayGroups.computeIfAbsent(angleSegment, k -> {
                Group g = new Group();
                g.setCache(true);
                g.setCacheHint(CacheHint.SPEED);
                return g;
            });

            group.getChildren().add(ray);
        }

        getChildren().addAll(rayGroups.values());

        this.setCache(true);
        this.setCacheHint(CacheHint.SPEED);
    }

    public LightRays(double centerRadius) {
        super();
        this.centerRadius = centerRadius;
        this.setCache(true);
        this.setCacheHint(CacheHint.SPEED);
        createRays();
        optimizeSceneGraph();
        startUnifiedAnimation();
    }

    private void createRays() {
        for (int i = 0; i < NUM_RAYS; i++) {
            double angle = 360.0 * i / NUM_RAYS;
            double initialLength = MIN_LENGTH + random.nextDouble() * (MAX_LENGTH - MIN_LENGTH);
            double thickness = 2 + random.nextDouble() * 4;

            Rectangle ray = new Rectangle(
                    centerRadius,
                    -thickness / 2.0,
                    initialLength,
                    thickness
            );
            ray.setArcWidth(thickness);
            ray.setArcHeight(thickness);
            ray.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.1 + random.nextDouble() * 0.4));

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
