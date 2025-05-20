package beat.osu.beatosu.view.landing.component;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LightRays extends Group {

    private final int NUM_RAYS = 300;
    private final double MIN_LENGTH = 0;
    private final double MAX_LENGTH = 100;
    private final double CENTER_RADIUS = 275;
    private final List<Rectangle> rays = new ArrayList<>();
    private final Random random = new Random();

    public LightRays() {
        createRays();
        startRandomAnimations();
    }

    private void createRays() {
        for (int i = 0; i < NUM_RAYS; i++) {
            double angle = 360.0 * i / NUM_RAYS;
            double initialLength = MIN_LENGTH + random.nextDouble() * (MAX_LENGTH - MIN_LENGTH);
            double thickness = 2 + random.nextDouble() * 4;

            // Rectangle starting at radius on positive X-axis, centered vertically
            Rectangle ray = new Rectangle(
                    CENTER_RADIUS,       // start x
                    -thickness / 2.0,    // start y
                    initialLength,       // width
                    thickness            // height
            );
            ray.setArcWidth(thickness);
            ray.setArcHeight(thickness);
            ray.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.1 + random.nextDouble() * 0.4));

            // Rotate about group origin (0,0)
            ray.getTransforms().add(new Rotate(angle, 0, 0));

            rays.add(ray);
            getChildren().add(ray);
        }
    }

    private void startRandomAnimations() {
        for (Rectangle ray : rays) {
            animateRay(ray);
        }
    }

    private void animateRay(Rectangle ray) {
        double newLength = MIN_LENGTH + random.nextDouble() * (MAX_LENGTH - MIN_LENGTH);
        Timeline timeline = new Timeline();
        Duration duration = Duration.millis(250 + random.nextDouble() * 250);
        KeyFrame keyFrame = new KeyFrame(duration,
                new KeyValue(ray.widthProperty(), newLength)
        );
        timeline.getKeyFrames().add(keyFrame);
        timeline.setOnFinished(e -> animateRay(ray));
        timeline.play();
    }

    public void pulseWithAudio(double intensity) {
        double scaleFactor = 1.0 + Math.min(intensity * 2.0, 1.0);
        for (Rectangle ray : rays) {
            ray.setWidth(ray.getWidth() * scaleFactor);
        }
    }
}
