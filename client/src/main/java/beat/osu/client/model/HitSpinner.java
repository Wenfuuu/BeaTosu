package beat.osu.client.model;

import beat.osu.client.Main;
import beat.osu.client.enums.HitResult;
import beat.osu.client.interfaces.game.HitObjectListener;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Objects;

public class HitSpinner extends HitObject{
    private long endTime;
    private boolean isActive = false;
    private HitObjectListener listener;

    private final Circle outerRing;
    private final Circle innerRing;
    private final Circle centerDot;
    private final ImageView spinnerImage;

    private RotateTransition spinAnimation;
    private FadeTransition hitEffectAnimation;
    private FadeTransition fadeInAnimation;

    private double currentRotation = 0;
    private long prevSpin = 0;
    private double completedSpins = 0;

    private double lastMouseAngle = 0;
    private boolean mousePressed = false;

    private double TARGET_SPINS;
    private final double ROTATION_SPEED = 1;

    public HitSpinner(int osuX, int osuY, long hitTime, int type, int hitSound,
                      String hitSample, long endTime, double approachRate, double circleSize,
                      double overallDifficulty, int comboNumber, int comboSetIndex, String colorString,
                      boolean comboEnd, ArrayList<String> sfxFilenames, HitObjectListener listener) {
        super(osuX, osuY, hitTime, type, hitSound, hitSample, approachRate, circleSize, comboNumber, comboSetIndex, comboEnd, sfxFilenames);
        Color circleColor = parseColorString(colorString);
        this.endTime = endTime;
        this.listener = listener;

        double baseRadius = getCircleRadius() * 2.5; // Spinners are larger than hit circles

        // Outer ring (approach circle equivalent)
        outerRing = new Circle(0, 0, baseRadius);
        outerRing.setFill(Color.TRANSPARENT);
        outerRing.setStroke(Color.WHITE);
        outerRing.setStrokeWidth(CIRCLE_STROKE_WIDTH);
        outerRing.getStrokeDashArray().addAll(15.0, 10.0); // Dashed border

        // Inner ring (main spinner area)
        innerRing = new Circle(0, 0, baseRadius * 0.8);
        innerRing.setFill(circleColor.deriveColor(1, 1, 1, 0.8));
        innerRing.setStroke(Color.WHITE);
        innerRing.setStrokeWidth(CIRCLE_STROKE_WIDTH);

        spinnerImage = new ImageView(new Image(Objects.requireNonNull
                (Main.class.getResourceAsStream("/assets/images/avatar-guest.png"))));
        spinnerImage.setFitWidth(baseRadius * 0.8);
        spinnerImage.setFitHeight(baseRadius * 0.8);
        spinnerImage.setPreserveRatio(true);
        spinnerImage.setLayoutX(-baseRadius * 0.4);
        spinnerImage.setLayoutY(-baseRadius * 0.4);

        // Center dot
        centerDot = new Circle(0, 0, 8);
        centerDot.setFill(Color.WHITE);
        centerDot.setStroke(Color.BLACK);
        centerDot.setStrokeWidth(2);

//        group = new Group(outerRing, innerRing, centerDot);
        group.getChildren().addAll(outerRing, innerRing, spinnerImage, centerDot);
        group.setUserData(this);

        calculateMinimumSpin(overallDifficulty);
        handleEvent();
    }

    private void calculateMinimumSpin(double overallDifficulty) {
        double spinnerDuration = (endTime - getHitTime()) / 1000.0;
        if (overallDifficulty < 5) {
            TARGET_SPINS = spinnerDuration * (1.5 + 0.2 * overallDifficulty) + 0.5;
        } else {
            TARGET_SPINS = spinnerDuration * (1.25 + 0.25 * overallDifficulty) + 0.5;
        }
    }

    private void handleEvent() {
        group.setOnMousePressed(event -> {
            if (isActive) {
                mousePressed = true;
                double deltaX = event.getX();
                double deltaY = event.getY();
                lastMouseAngle = Math.atan2(deltaY, deltaX);
//                startSpinning();
            }
        });

        group.setOnMouseDragged(event -> {
            if (mousePressed && isActive) {
                double deltaX = event.getX();
                double deltaY = event.getY();
                double currentMouseAngle = Math.atan2(deltaY, deltaX);
                double angleDiff = currentMouseAngle - lastMouseAngle;

                if (angleDiff > Math.PI) {
                    angleDiff -= 2 * Math.PI;
                } else if (angleDiff < -Math.PI) {
                    angleDiff += 2 * Math.PI;
                }
                double degreesRotated = Math.toDegrees(Math.abs(angleDiff));
                addRotation(degreesRotated);

                lastMouseAngle = currentMouseAngle;
            }
        });

        group.setOnMouseReleased(event -> {
            mousePressed = false;
//            stopSpinning();
        });
    }

    @Override
    public Node getNode() {
        return group;
    }

    @Override
    public void update(long currentTime) {
        long timeUntilHit = getHitTime() - currentTime;// time left for perfect hit

        if (!isVisible() && timeUntilHit <= getPreempt()) {
            appear();
        }

        if (currentTime >= getHitTime() && currentTime <= endTime) {
            isActive = true;
        }

        if (currentTime > endTime) {
            setHit(true);
            isActive = false;
            setVisible(false);
            playMissEffect();
        }
    }

    public void addRotation(double degrees) {
        if (isActive) {
            degrees *= ROTATION_SPEED;

            currentRotation += degrees;
            completedSpins = currentRotation / 360.0;

            // Rotate the inner ring visually
            innerRing.setRotate(innerRing.getRotate() + degrees);
            spinnerImage.setRotate(spinnerImage.getRotate() + degrees);
        }
    }

    public void updateSpinner(double mouseX, double mouseY) {
        if(isHit() && isActive) {
             double relativeX = mouseX - group.getLayoutX();
             double relativeY = mouseY - group.getLayoutY();

            double currentMouseAngle = Math.atan2(relativeY, relativeX);
            double angleDiff = currentMouseAngle - lastMouseAngle;

            // Normalize angle difference
            if (angleDiff > Math.PI) {
                angleDiff -= 2 * Math.PI;
            } else if (angleDiff < -Math.PI) {
                angleDiff += 2 * Math.PI;
            }

            // Only add rotation if there's actual mouse movement
            if (Math.abs(angleDiff) > 0.001) { // Small threshold to avoid jitter
                double degreesRotated = Math.toDegrees(Math.abs(angleDiff));
                addRotation(degreesRotated);
                lastMouseAngle = currentMouseAngle;
            }

            if(prevSpin < completedSpins - 1) {
                System.out.println("current completed rotations: " + completedSpins);
                prevSpin = Math.round(completedSpins);
                System.out.println("previous completed rotations: " + prevSpin);
                listener.onHit(this, HitResult.SPIN);
                if(prevSpin > TARGET_SPINS) {
                    listener.onHit(this, HitResult.COMPLETE_SPIN);
                    int totalRotation = (int) prevSpin;
                    listener.onAdditionalSpin(this, totalRotation - (int) TARGET_SPINS);
                }
            }
        }else if(isHit() && !isActive && !isVisible()) {
            // check & notify judgement score
            if(completedSpins >= TARGET_SPINS) {
                listener.onHit(this, HitResult.PERFECT);
            }else if(completedSpins >= TARGET_SPINS - 1) {
                listener.onHit(this, HitResult.GREAT);
            } else if(completedSpins >= TARGET_SPINS * 0.25) {
                listener.onHit(this, HitResult.GOOD);
            } else {
                listener.onMiss(this);
            }
        }
    }

    @Override
    public void playAppearAnimation() {
        fadeInAnimation = new FadeTransition(Duration.millis(getFadeIn()), group);
        fadeInAnimation.setFromValue(0);
        fadeInAnimation.setToValue(1);
        fadeInAnimation.play();
    }

    @Override
    public void playHitEffect() {

    }

    @Override
    public void playMissEffect() {
        FadeTransition hideAnimation = new FadeTransition(Duration.millis(150), group);
        hideAnimation.setToValue(0);
        hideAnimation.setOnFinished(e -> {
            hide();
        });
        hideAnimation.play();
    }

    @Override
    public void applyVisualsToNode(double centerX, double centerY, double scaledRadius) {
        if (group != null) {
            // Position the Group at the center of the screen (spinners are always centered)
            group.setLayoutX(centerX);
            group.setLayoutY(centerY);

            // Scale the spinner elements based on the scale factor
            double baseRadius = scaledRadius * 2.5; // Spinners are larger
            outerRing.setRadius(baseRadius);
            innerRing.setRadius(baseRadius * 0.8);
        }
    }

    @Override
    public void pauseAnimations() {
        if (spinAnimation != null && spinAnimation.getStatus() == Animation.Status.RUNNING) {
            spinAnimation.pause();
        }
        if (hitEffectAnimation != null && hitEffectAnimation.getStatus() == Animation.Status.RUNNING) {
            hitEffectAnimation.pause();
        }
        if (fadeInAnimation != null && fadeInAnimation.getStatus() == Animation.Status.RUNNING) {
            fadeInAnimation.pause();
        }
    }

    @Override
    public void resumeAnimations() {
        if (spinAnimation != null && spinAnimation.getStatus() == Animation.Status.PAUSED) {
            spinAnimation.play();
        }
        if (hitEffectAnimation != null && hitEffectAnimation.getStatus() == Animation.Status.PAUSED) {
            hitEffectAnimation.play();
        }
        if (fadeInAnimation != null && fadeInAnimation.getStatus() == Animation.Status.PAUSED) {
            fadeInAnimation.play();
        }
    }
}
