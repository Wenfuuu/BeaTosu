package beat.osu.client.model;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;

public class HitSpinner extends HitObject{
    private long endTime;

    private final Group group;
    private final Circle outerRing;
    private final Circle innerRing;
    private final Circle centerDot;

    private RotateTransition spinAnimation;
    private ScaleTransition approachAnimation;
    private FadeTransition hitEffectAnimation;
    private FadeTransition fadeInAnimation;

    private double currentRotation = 0;
    private double targetRotations = 0;
    private double completedRotations = 0;
    private boolean isSpinning = false;
    private boolean isCompleted = false;

    public HitSpinner(int osuX, int osuY, long hitTime, int type, int hitSound,
                      String hitSample, long endTime, double approachRate, double circleSize,
                      int comboNumber, int comboSetIndex, String colorString, boolean comboEnd,
                      ArrayList<String> sfxFilenames) {
        super(osuX, osuY, hitTime, type, hitSound, hitSample, approachRate, circleSize, comboNumber, comboSetIndex, comboEnd, sfxFilenames);
        Color circleColor = parseColorString(colorString);
        this.endTime = endTime;

        long duration = endTime - hitTime;
        this.targetRotations = Math.max(3, duration / 1000.0 * 2.0);

        double baseRadius = getCircleRadius() * 2.5; // Spinners are larger than hit circles

        // Outer ring (approach circle equivalent)
        outerRing = new Circle(0, 0, baseRadius);
        outerRing.setFill(Color.TRANSPARENT);
        outerRing.setStroke(Color.WHITE);
//        outerRing.setStrokeWidth(CIRCLE_STROKE_WIDTH * 2);
        outerRing.setStrokeWidth(CIRCLE_STROKE_WIDTH);
        outerRing.getStrokeDashArray().addAll(15.0, 10.0); // Dashed border

        // Inner ring (main spinner area)
        innerRing = new Circle(0, 0, baseRadius * 0.8);
        innerRing.setFill(circleColor.deriveColor(1, 1, 1, 0.8));
        innerRing.setStroke(Color.WHITE);
        innerRing.setStrokeWidth(CIRCLE_STROKE_WIDTH);

        // Center dot
        centerDot = new Circle(0, 0, 8);
        centerDot.setFill(Color.WHITE);
        centerDot.setStroke(Color.BLACK);
        centerDot.setStrokeWidth(2);

        group = new Group(outerRing, innerRing, centerDot);
        group.setVisible(false);
        group.setUserData(this);
    }

    private void playFadeInAnimation() {
        group.setOpacity(0);
        fadeInAnimation = new FadeTransition(Duration.millis(getFadeIn()), group);
        fadeInAnimation.setFromValue(0);
        fadeInAnimation.setToValue(1);
        fadeInAnimation.play();
    }

    @Override
    public Node getNode() {
        return group;
    }

    @Override
    public void update(long currentTime) {

    }

    @Override
    public void playHitEffect() {

    }

    @Override
    public void applyVisualsToNode(double centerX, double centerY, double scaledRadius) {

    }

    @Override
    public void appear() {
        if(!isVisible()) {
            setVisible(true);
            group.setVisible(true); // Make the whole group visible
            playFadeInAnimation();
        }
    }

    @Override
    public void hide() {

    }

    @Override
    public void pauseAnimations() {

    }

    @Override
    public void resumeAnimations() {

    }
}
