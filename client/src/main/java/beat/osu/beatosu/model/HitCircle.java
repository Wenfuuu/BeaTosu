package beat.osu.beatosu.model;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class HitCircle extends HitObject{

    private final Group group;
    private final Circle outerCircle;

    // Define visual constants (could be based on CS later)
    private static final double INNER_RADIUS = 40;
    private static final double OUTER_RADIUS_START_SCALE = 5.0;

    public HitCircle(int osuX, int osuY, long hitTime, int type, int hitSound, String hitSample, double approachRate) {
        super(osuX, osuY, hitTime, type, hitSound, hitSample, approachRate);

        // --- CORE CHANGE: Create circles at (0,0) relative to the Group ---
        Circle innerCircle = new Circle(0, 0, INNER_RADIUS);
        innerCircle.setFill(Color.rgb(100, 180, 255, 0.8)); // Example color
        innerCircle.setStroke(Color.WHITE);
        innerCircle.setStrokeWidth(2);

        outerCircle = new Circle(0, 0, INNER_RADIUS); // Base radius, will be scaled by animation
        outerCircle.setFill(Color.TRANSPARENT);
        outerCircle.setStroke(Color.WHITE); // Approach circle color
        outerCircle.setStrokeWidth(2);

        group = new Group(outerCircle, innerCircle);
        group.setVisible(false); // Start invisible

        // --- CORE CHANGE: Link the Group node back to this HitCircle object ---
        group.setUserData(this);

        handleEvent();
    }

    private void appear() {
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

        ScaleTransition scale = new ScaleTransition(Duration.millis(getPreempt()), outerCircle);
        scale.setFromX(OUTER_RADIUS_START_SCALE);
        scale.setFromY(OUTER_RADIUS_START_SCALE);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }

    private void playHitEffect() {
        FadeTransition fade = new FadeTransition(Duration.millis(150), group);
        fade.setToValue(0);
        // Remove from parent pane after fade out to clean up
        fade.setOnFinished(e -> {
            hide();
        });
        fade.play();
    }

    public void hide() {
        // Could add a fade out here too for misses
        group.setVisible(false);
        // remove from parent pane on hide/miss as well
        if(group.getParent() instanceof Pane) {
            ((Pane) group.getParent()).getChildren().remove(group);
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

        // Auto-miss logic (adjust timing as needed)
        if (isVisible() && !isHit() && timeUntilHit < -200) { // Allow some time after hitTime
//            System.out.println("Missed: " + getOsuX() + "," + getOsuY() + " at " + currentTime + "ms");
            hide();
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
    public void setPosition(double paneX, double paneY) {
        if(group != null) {
            group.relocate(paneX, paneY);
        }
    }
}
