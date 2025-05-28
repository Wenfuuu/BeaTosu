package beat.osu.client.model;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;

public class HitCircle extends HitObject{

    private final Group group;
    private final Circle innerCircle;
    private final Circle outerCircle;
    private final Label comboLabel;

    private ScaleTransition approachAnimation;
    private FadeTransition hitEffectAnimation;

    // Define visual constants (could be based on CS later)
    private static final double OUTER_RADIUS_START_SCALE = 5.0;

    public HitCircle(int osuX, int osuY, long hitTime,
                     int type, int hitSound, String hitSample,
                     double approachRate, double circleSize,
                     int comboNumber, int comboSetIndex, String colorString,
                     ArrayList<String> sfxFilenames) {
        super(osuX, osuY, hitTime, type, hitSound, hitSample, approachRate,
                circleSize, comboNumber, comboSetIndex, sfxFilenames);

        Color circleColor = parseColorString(colorString);
        // --- CORE CHANGE: Create circles at (0,0) relative to the Group ---
        innerCircle = new Circle(0, 0, getCircleRadius());
        innerCircle.setFill(circleColor.deriveColor(1, 1, 1, 0.8)); // Example color
        innerCircle.setStroke(Color.WHITE);
        innerCircle.setStrokeWidth(2);

        outerCircle = new Circle(0, 0, getCircleRadius()); // Base radius, will be scaled by animation
        outerCircle.setFill(Color.TRANSPARENT);
        outerCircle.setStroke(Color.WHITE); // Approach circle color
        outerCircle.setStrokeWidth(2);

        comboLabel = new Label(String.valueOf(getComboNumber()));
        comboLabel.setFont(new Font(50));
        comboLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        comboLabel.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            comboLabel.setLayoutX(-newBounds.getWidth() / 2);
            comboLabel.setLayoutY(-newBounds.getHeight() / 2);
        });

        group = new Group(outerCircle, innerCircle, comboLabel);
        group.setVisible(false); // Start invisible

        // --- CORE CHANGE: Link the Group node back to this HitCircle object ---
        group.setUserData(this);

        handleEvent();
    }

    @Override
    public void appear() {
        if(!isVisible()) {
            setVisible(true);
            group.setVisible(true); // Make the whole group visible
            playApproachAnimation();
        }
    }

    private void playApproachAnimation() {
        // Make sure outer circle exists
        if(outerCircle == null) return;

        // Reset scale before playing (in case updateLayout runs mid-animation)
        outerCircle.setScaleX(OUTER_RADIUS_START_SCALE);
        outerCircle.setScaleY(OUTER_RADIUS_START_SCALE);

        approachAnimation = new ScaleTransition(Duration.millis(getPreempt()), outerCircle);
        approachAnimation.setFromX(OUTER_RADIUS_START_SCALE);
        approachAnimation.setFromY(OUTER_RADIUS_START_SCALE);
        approachAnimation.setToX(1.0);
        approachAnimation.setToY(1.0);
        approachAnimation.play();
    }

    @Override
    public void playHitEffect() {
        hitEffectAnimation = new FadeTransition(Duration.millis(150), group);
        hitEffectAnimation.setToValue(0);
        // Remove from parent pane after fade out to clean up
        hitEffectAnimation.setOnFinished(e -> {
            hide();
        });
        hitEffectAnimation.play();
    }

    @Override
    public void hide() {
        // Could add a fade out here too for misses
        setVisible(false);
        group.setVisible(false);
        // remove from parent pane on hide/miss as well
        if(group.getParent() instanceof Pane) {
            ((Pane) group.getParent()).getChildren().remove(group);
        }
    }

    @Override
    public void pauseAnimations() {
        if(approachAnimation != null && approachAnimation.getStatus() == Animation.Status.RUNNING) {
            approachAnimation.pause();
        }
        if(hitEffectAnimation != null && hitEffectAnimation.getStatus() == Animation.Status.RUNNING) {
            hitEffectAnimation.pause();
        }
    }

    @Override
    public void resumeAnimations() {
        if(approachAnimation != null && approachAnimation.getStatus() == Animation.Status.PAUSED) {
            approachAnimation.play();
        }
        if(hitEffectAnimation != null && hitEffectAnimation.getStatus() == Animation.Status.PAUSED) {
            hitEffectAnimation.play();
        }
    }

    @Override
    public Node getNode() {
        return group;
    }

    @Override
    public void update(long currentTime) {
        long timeUntilHit = getHitTime() - currentTime;// time left for perfect hit

        // appear based on preempt time
        if (!isVisible() && timeUntilHit <= getPreempt()) {
            appear();
        }
    }

    @Override
    public void handleEvent() {
        group.setOnMouseClicked(e -> { // Use group to catch clicks slightly outside innerCircle
            if (isVisible() && !isHit()) {
                long clickTime = getCurrTime();
                long timingError = clickTime - getHitTime(); // Calculate hit timing
                setHit(true);
                playHitEffect();
//                System.out.println("Hit: " + getOsuX() + "," + getOsuY() + " | Timing: " + timingError + "ms");
                // calculate score based on timingError here
            }
        });
    }

    @Override
    public void applyVisualsToNode(double centerX, double centerY, double scaledRadius) {
        if (group != null) {
            // Position the Group so its local (0,0) point (which is the center of the circles)
            // is at (centerX, centerY) on the pane.
            group.setLayoutX(centerX);
            group.setLayoutY(centerY);

            // Update the radius of the circles based on the scaleFactor
            innerCircle.setRadius(scaledRadius);
            outerCircle.setRadius(scaledRadius); // Approach circle's base radius is also scaled

            // Optional: Scale stroke width if desired.
            // For a consistent look, stroke width might also need to be scaled.
            // E.g., double newStrokeWidth = 2.0 * (scaledRadius / getUnscaledRadius());
            // if (getUnscaledRadius() > 0) { // Avoid division by zero
            //     innerCircle.setStrokeWidth(Math.max(1.0, newStrokeWidth));
            //     outerCircle.setStrokeWidth(Math.max(1.0, newStrokeWidth));
            // }
        }
    }
}
