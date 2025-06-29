package beat.osu.client.model;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;

public class HitCircle extends HitObject{

    private final Circle innerCircle;
    private final Circle outerCircle;
    private final Label comboLabel;

    private FadeTransition hitEffectAnimation;
    private ParallelTransition parallelAnimation;

    public HitCircle(int osuX, int osuY, long hitTime,
                     int type, int hitSound, String hitSample,
                     double approachRate, double circleSize,
                     int comboNumber, int comboSetIndex, String colorString,
                     boolean comboEnd,
                     ArrayList<String> sfxFilenames) {
        super(osuX, osuY, hitTime, type, hitSound, hitSample, approachRate,
                circleSize, comboNumber, comboSetIndex, comboEnd, sfxFilenames);

        Color circleColor = parseColorString(colorString);
        // --- CORE CHANGE: Create circles at (0,0) relative to the Group ---
        innerCircle = new Circle(0, 0, getCircleRadius());
        innerCircle.setFill(circleColor.deriveColor(1, 1, 1, 0.8)); // Example color
        innerCircle.setStroke(Color.WHITE);
        innerCircle.setStrokeWidth(CIRCLE_STROKE_WIDTH);

        outerCircle = new Circle(0, 0, getCircleRadius()); // Base radius, will be scaled by animation
        outerCircle.setFill(Color.TRANSPARENT);
        outerCircle.setStroke(Color.WHITE); // Approach circle color
        outerCircle.setStrokeWidth(CIRCLE_STROKE_WIDTH);

        comboLabel = new Label(String.valueOf(getComboNumber()));
        comboLabel.setFont(Font.font("Aller", 50));
        comboLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        comboLabel.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            comboLabel.setLayoutX(-newBounds.getWidth() / 2);
            comboLabel.setLayoutY(-newBounds.getHeight() / 2);
        });

//        group = new Group(outerCircle, innerCircle, comboLabel);
//        group.setVisible(false); // Start invisible
        group.getChildren().addAll(outerCircle, innerCircle, comboLabel);

        // --- CORE CHANGE: Link the Group node back to this HitCircle object ---
        group.setUserData(this);
    }

    @Override
    public void playAppearAnimation() {
        // Reset scale before playing (in case updateLayout runs mid-animation)
        outerCircle.setScaleX(APPROACH_START_SCALE);
        outerCircle.setScaleY(APPROACH_START_SCALE);

        ScaleTransition approachAnimation = new ScaleTransition(Duration.millis(getPreempt()), outerCircle);
        approachAnimation.setFromX(APPROACH_START_SCALE);
        approachAnimation.setFromY(APPROACH_START_SCALE);
        approachAnimation.setToX(1.0);
        approachAnimation.setToY(1.0);

        FadeTransition fadeInAnimation = new FadeTransition(Duration.millis(getFadeIn()), group);
        fadeInAnimation.setFromValue(0);
        fadeInAnimation.setToValue(1);

        parallelAnimation = new ParallelTransition(approachAnimation, fadeInAnimation);
        parallelAnimation.play();
    }

    @Override
    public void playHitEffect() {
        if (parallelAnimation != null) parallelAnimation.stop();
        hitEffectAnimation = new FadeTransition(Duration.millis(150), group);
        hitEffectAnimation.setToValue(0);
        hitEffectAnimation.setOnFinished(e -> {
            hide();
        });
        hitEffectAnimation.play();
    }

    @Override
    public void playMissEffect() {
        if (parallelAnimation != null) parallelAnimation.stop();
        hitEffectAnimation = new FadeTransition(Duration.millis(150), group);
        hitEffectAnimation.setToValue(0);
        hitEffectAnimation.setOnFinished(e -> {
            hide();
        });
        hitEffectAnimation.play();
    }

    @Override
    public void pauseAnimations() {
        if(parallelAnimation != null && parallelAnimation.getStatus() == Animation.Status.RUNNING) {
            parallelAnimation.pause();
        }
        if(hitEffectAnimation != null && hitEffectAnimation.getStatus() == Animation.Status.RUNNING) {
            hitEffectAnimation.pause();
        }
    }

    @Override
    public void resumeAnimations() {
        if(parallelAnimation != null && parallelAnimation.getStatus() == Animation.Status.PAUSED) {
            parallelAnimation.play();
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
    public void applyVisualsToNode(double centerX, double centerY, double scaledRadius) {
        if (group != null) {
            // Position the Group so its local (0,0) point (which is the center of the circles)
            // is at (centerX, centerY) on the pane.
            group.setLayoutX(centerX);
            group.setLayoutY(centerY);

            // Update the radius of the circles based on the scaleFactor
            innerCircle.setRadius(scaledRadius);
            outerCircle.setRadius(scaledRadius); // Approach circle's base radius is also scaled
        }
    }
}
