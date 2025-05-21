package beat.osu.beatosu.model;

import beat.osu.beatosu.utils.OsuParser;
import javafx.animation.ScaleTransition;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class HitSlider extends HitObject {

    private final Group group;
    private final Circle headCircle;
    private Path sliderPath;
    private final Circle sliderBall;
    private final Circle approachCircle;

    // Parsed Slider Data - To be populated by parseSliderParams
    private char sliderType = '?'; // Default value
    private List<Point2D> controlPoints = new ArrayList<>(); // Initialize
    private int repeats = 1; // Default value
    private double pixelLength = 0.0; // Default value
    private String edgeSoundsStr = ""; // Store raw string for now
    private String edgeSetsStr = "";   // Store raw string for now

    // Timing & Animation
    private long endTime;
    private double duration;
    private boolean headHit = false;
    private ScaleTransition approachAnimation; // Store the animation

    // Visual Constants
    private static final double CIRCLE_RADIUS = 40;
    private static final double PATH_STROKE_WIDTH = CIRCLE_RADIUS * 2;
    private static final double BALL_RADIUS = CIRCLE_RADIUS * 0.8;
    private static final double APPROACH_START_SCALE = 3.0; // How big approach circle starts
    private static final double APPROACH_STROKE_WIDTH = 3.0;

    private double calculateSliderDuration(double sliderMultiplier, ArrayList<TimingPoint> timingPoints) {
        TimingPoint inherited = null;
        TimingPoint uninherited = null;

        for (TimingPoint tp : timingPoints) {
            if (tp.getTime() > this.getHitTime()) break;

            if (!tp.isInherited()) {
                // Store the last uninherited point before hitTime
                if (uninherited == null || tp.getTime() > uninherited.getTime()) {
                    uninherited = tp;
                }
            } else {
                // Store the last inherited point before hitTime
                if (inherited == null || tp.getTime() > inherited.getTime()) {
                    inherited = tp;
                }
            }
        }

        double SV, beatLength;
        if (inherited != null) {
            beatLength = inherited.getBeatLength(); // Negative
            SV = sliderMultiplier * (100.0 / -beatLength);
        } else {
            // No inherited point — fallback
            SV = 1.0; // Default SV (slider velocity multiplier)
            beatLength = uninherited != null ? uninherited.getBeatLength() : 500.0; // Fallback to 120 BPM
        }

        return (pixelLength / (100.0 * SV)) * beatLength;
    }

    private void parseSliderParams(String paramsStr, int startX, int startY) {
        // Split the main components separated by commas
        String[] mainParts = paramsStr.split(",");
        // part[0] => type|points
        // part[1] => repeat
        // part[2] => pixel length
        // part[3] => edge sounds
        // part[4] => edge sets

        // --- Part 0: Curve Data (curveType|curvePoints) ---
        String curveData = mainParts[0];
        String[] curveParts = curveData.split("\\|"); // Split curveType from points
//        for(String part : curveParts) {
//            System.out.println(part);
//        }
        if (curveParts.length > 0) {
            this.sliderType = curveParts[0].isEmpty() ? '?' : curveParts[0].charAt(0);
        }

        // Always add the slider's own start point first
        this.controlPoints.clear(); // Ensure list is empty before adding
        this.controlPoints.add(new Point2D(startX, startY));

        // Add the subsequent curve points
        if (curveParts.length > 1) {
            for (int i = 1; i < curveParts.length; i++) {
                String[] coords = curveParts[i].split(":");
                if (coords.length == 2) {
                    try {
                        int px = Integer.parseInt(coords[0]);
                        int py = Integer.parseInt(coords[1]);
                        this.controlPoints.add(new Point2D(px, py));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing slider control point coordinates: " + curveParts[i] + " in " + paramsStr);
                    }
                } else {
                    System.err.println("Warning: Invalid control point format (expected x:y): " + curveParts[i] + " in " + paramsStr);
                }
            }
        }

        // Repeat
        if(!mainParts[1].isEmpty()) {
            this.repeats = Integer.parseInt(mainParts[1]);
        }else this.repeats = 1;

        // Pixel Length
        if(mainParts.length > 2 && !mainParts[2].isEmpty()){
            this.pixelLength = Double.parseDouble(mainParts[2]);
        }else this.pixelLength = 0.0;

        // --- Part 3: Edge Sounds (Optional) ---
        if (mainParts.length > 3) {
            this.edgeSoundsStr = mainParts[3];
            // TODO: Parse edgeSoundsStr further if needed (e.g., into a list of integers)
        }

        // --- Part 4: Edge Sets (Optional) ---
        if (mainParts.length > 4) {
            this.edgeSetsStr = mainParts[4];
            // TODO: Parse edgeSetsStr further if needed (e.g., into lists of "normal:addition" pairs)
        }
    }

    public HitSlider(int osuX, int osuY, long hitTime, int type, int hitSound,
                     String objectParams, String hitSample, double approachRate,
                     double sliderMultiplier) {
        super(osuX, osuY, hitTime, type, hitSound, hitSample, approachRate);

         parseSliderParams(objectParams, getOsuX(), getOsuY());

        // duration
        this.duration = calculateSliderDuration(sliderMultiplier, OsuParser.getTimingPointsList());
        this.endTime = getHitTime() + (long) (this.duration * this.repeats);

        group = new Group();
        group.setVisible(false);

//        sliderPath = createSliderPathVisual(); // Uses parsed controlPoints
//        if (sliderPath != null) {
//            group.getChildren().add(sliderPath);
//        }

        headCircle = new Circle(0, 0, CIRCLE_RADIUS);
        headCircle.setFill(Color.rgb(100, 180, 255, 0.8));
        headCircle.setStroke(Color.WHITE);
        headCircle.setStrokeWidth(2);
        group.getChildren().add(headCircle);

        // 3. Approach Circle (Added)
        approachCircle = new Circle(0, 0, CIRCLE_RADIUS); // Same base radius as head
        approachCircle.setFill(Color.TRANSPARENT);
        approachCircle.setStroke(Color.WHITE);
        approachCircle.setStrokeWidth(APPROACH_STROKE_WIDTH);
        approachCircle.setScaleX(APPROACH_START_SCALE); // Start scaled up
        approachCircle.setScaleY(APPROACH_START_SCALE);
        group.getChildren().add(approachCircle); // Add to group

        sliderBall = new Circle(0, 0, BALL_RADIUS);
        sliderBall.setFill(Color.WHITE.deriveColor(1, 1, 1, 0.7));
        sliderBall.setVisible(false);// set back to false later
        group.getChildren().add(sliderBall);

        group.setUserData(this);

        handleEvent();
    }

    private void appear() {
        if(!isVisible()) {
            setVisible(true);
            group.setVisible(true);
            playApproachAnimation();
        }
    }

    private void playApproachAnimation() {
        if (approachCircle == null || headHit) return; // Don't play if already hit

        approachCircle.setVisible(true); // Make sure it's visible
        // Reset scale just in case
        approachCircle.setScaleX(APPROACH_START_SCALE);
        approachCircle.setScaleY(APPROACH_START_SCALE);

        approachAnimation = new ScaleTransition(Duration.millis(getPreempt()), approachCircle);
        approachAnimation.setFromX(APPROACH_START_SCALE);
        approachAnimation.setFromY(APPROACH_START_SCALE);
        approachAnimation.setToX(1.0);
        approachAnimation.setToY(1.0);
//        approachAnimation.setOnFinished(e -> {
//            // Optionally hide approach circle slightly after hitTime if not hit?
//            if (!headHit) {
//                // approachCircle.setVisible(false); // Or just leave it until miss
//            }
//        });
        approachAnimation.play();
    }

    private void hide() {
        // add fade out later
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

    }

    @Override
    public void handleEvent() {

    }

    @Override
    public void setPosition(double paneX, double paneY) {
        if(group != null) {
            group.relocate(paneX, paneY);
        }
    }
}
