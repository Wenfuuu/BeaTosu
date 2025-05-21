package beat.osu.beatosu.model;

import beat.osu.beatosu.utils.OsuParser;
import javafx.animation.ScaleTransition;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class HitSlider extends HitObject {

    private final Group group;
    private final Circle headCircle;
    private final Path sliderPath;
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
    private static final double APPROACH_START_SCALE = 5.0; // How big approach circle starts
    private static final double APPROACH_STROKE_WIDTH = 2.0;

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

        return (pixelLength / (100.0 * SV)) * Math.abs(beatLength);
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
        this.duration = calculateSliderDuration(sliderMultiplier, OsuParser.getTimingPointsList()) * 10; // Convert to milliseconds
        this.endTime = getHitTime() + (long) (this.duration * this.repeats);

        group = new Group();
        group.setVisible(false);

        sliderPath = createSliderPath(); // Uses parsed controlPoints
        if (sliderPath != null) {
            group.getChildren().add(sliderPath);
        }

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

    private Path createSliderPath() {
        if (controlPoints.isEmpty()) return null;

        Path path = new Path();
        path.setStroke(Color.BLUE);
        path.setStrokeWidth(PATH_STROKE_WIDTH);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);

        Point2D start = controlPoints.get(0); // The actual start coordinate
        path.getElements().add(new MoveTo(0, 0)); // Path starts at group origin (0,0)

        // linear first
        for (int i = 1; i < controlPoints.size(); i++) {
            Point2D p = controlPoints.get(i);
            path.getElements().add(new LineTo(p.getX() - start.getX(), p.getY() - start.getY()));
        }
        return path;
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

    private double getBallFraction(double timeSinceHit) {
        double totalDuration = this.duration * this.repeats; // Total time for all slides
        if (totalDuration <= 0) totalDuration = 1; // Avoid division by zero

        double fractionElapsed = timeSinceHit / totalDuration;

        int currentSegment = (int) Math.floor(fractionElapsed * this.repeats); // Which traversal (0, 1, 2...)
        // Correct fraction within the current 0-1 traversal
        double fractionInCurrentTraversal = (fractionElapsed * this.repeats) - currentSegment;

        boolean isReverse = (currentSegment % 2) != 0;

        double ballFraction = isReverse ? (1.0 - fractionInCurrentTraversal) : fractionInCurrentTraversal;
        ballFraction = Math.max(0.0, Math.min(1.0, ballFraction)); // Clamp
        return ballFraction;
    }

    private Point2D getPointOnLinear(Point2D p0, Point2D p1, double t) {
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        double x = (1.0 - t) * p0.getX() + t * p1.getX();
        double y = (1.0 - t) * p0.getY() + t * p1.getY();
        return new Point2D(x, y);
    }

    private Point2D getVisualPointAtFraction(double fraction) {
        if (controlPoints.size() < 2) return new Point2D(0, 0); // No path

        Point2D sliderStartAbs = controlPoints.get(0);
        Point2D interpolatedAbsolutePoint;

        // linear paths with multiple segments
        if (controlPoints.size() > 2) {
            // Calculate total path length
            double totalLength = 0;
            double[] segmentLengths = new double[controlPoints.size() - 1];

            for (int i = 0; i < controlPoints.size() - 1; i++) {
                double segmentLength = controlPoints.get(i).distance(controlPoints.get(i + 1));
                segmentLengths[i] = segmentLength;
                totalLength += segmentLength;
            }

            // Find which segment the fraction falls on
            double targetDistance = fraction * totalLength;
            double distanceAccumulated = 0;
            int segmentIndex = 0;

            for (int i = 0; i < segmentLengths.length; i++) {
                if (distanceAccumulated + segmentLengths[i] >= targetDistance) {
                    segmentIndex = i;
                    break;
                }
                distanceAccumulated += segmentLengths[i];
            }

            // Calculate fraction within the segment
            double segmentFraction = segmentIndex < segmentLengths.length ?
                    (targetDistance - distanceAccumulated) / segmentLengths[segmentIndex] : 1.0;

            // Interpolate within the segment
            Point2D p0 = controlPoints.get(segmentIndex);
            Point2D p1 = controlPoints.get(segmentIndex + 1);

            double interpX = p0.getX() * (1 - segmentFraction) + p1.getX() * segmentFraction;
            double interpY = p0.getY() * (1 - segmentFraction) + p1.getY() * segmentFraction;

            // Return relative to group coordinates
            return new Point2D(interpX - sliderStartAbs.getX(), interpY - sliderStartAbs.getY());
        } else {
            // Simple linear interpolation between first and last point
            return getPointOnLinear(controlPoints.get(0), controlPoints.get(controlPoints.size()-1), fraction)
                    .subtract(sliderStartAbs);
        }
    }

    @Override
    public Node getNode() {
        return group;
    }

    @Override
    public void update(long currentTime) {
        setCurrTime(currentTime);
        long timeUntilHit = getHitTime() - getCurrTime();// time left for perfect hit
        long timeSinceHit = getCurrTime() - getHitTime();

        // appear based on preempt time
        if (!isVisible() && timeUntilHit <= getPreempt()) {
            appear();
        }

        // add ball movement here
        if (headHit && getCurrTime() <= endTime) {
            double ballFraction = getBallFraction((double) timeSinceHit);

            Point2D ballPos = getVisualPointAtFraction(ballFraction);
            sliderBall.setCenterX(ballPos.getX());
            sliderBall.setCenterY(ballPos.getY());
        } else if (headHit && currentTime > endTime) {
            hide(); // Slider finished
        }

        // miss logic (adjust timing later)
        if (isVisible() && !isHit() && timeUntilHit < -200) { // Allow some time after hitTime
//            System.out.println("Missed: " + getOsuX() + "," + getOsuY() + " at " + currentTime + "ms");
            hide();
        }
    }

    @Override
    public void handleEvent() {
        group.setOnMouseClicked(e -> { // Changed to group
            if (isVisible() && !headHit && e.getTarget() != sliderBall) { // Prevent clicks on ball itself re-triggering
                long clickTime = getCurrTime();
                long timingError = clickTime - getHitTime();
                // Check if click is within hit window AND near the head circle visually
                Point2D clickInGroup = new Point2D(e.getX(), e.getY());
                if (Math.abs(timingError) < 200 && clickInGroup.distance(0,0) <= CIRCLE_RADIUS * 1.5) { // Allow slightly larger hit area
                    System.out.println("hitting the slider");
                    System.out.println("slider duration is " + this.duration * this.repeats);
                    headHit = true;
                    approachCircle.setVisible(false); // Hide approach circle
                    if (approachAnimation != null) approachAnimation.stop(); // Stop animation if running
                    headCircle.setVisible(false); // Hide static head circle
                    sliderBall.setVisible(true);  // Show the moving ball
//                    System.out.println("Slider Head Hit: " + this.x + "," + this.y + " | Timing: " + timingError + "ms");
                } else {
//                    System.out.println("Slider Head Miss (Timing/Position): " + timingError + "ms, Dist: " + clickInGroup.distance(0,0));
                    hide();
                }
            }
        });
    }

    @Override
    public void setPosition(double paneX, double paneY) {
        if(group != null) {
//            group.relocate(paneX, paneY);
            group.relocate(paneX - CIRCLE_RADIUS * 2, paneY - CIRCLE_RADIUS * 2);
        }
    }
}
