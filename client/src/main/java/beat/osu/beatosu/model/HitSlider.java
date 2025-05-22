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

    // Parsed Slider Data
    private char sliderType = '?';
    private List<Point2D> controlPoints = new ArrayList<>();
    private int repeats = 1; // This is the number of "repeats" AFTER the initial slide. Total traversals = repeats + 1.
    private double pixelLength = 0.0;
    private String edgeSoundsStr = "";
    private String edgeSetsStr = "";

    // Timing & Animation
    private long endTime;
    private double duration; // Duration of a SINGLE traversal of the slider in milliseconds
    private boolean headHit = false;
    private ScaleTransition approachAnimation;

    // Visual Constants
    private final double PATH_STROKE_WIDTH;
    private final double BALL_RADIUS;
    private static final double APPROACH_START_SCALE = 5.0;
    private static final double APPROACH_STROKE_WIDTH = 2.0;

    private double calculateSliderDuration(double baseSliderMultiplier, ArrayList<TimingPoint> timingPoints) {
        TimingPoint activeUninheritedTP = null;
        TimingPoint lastRelevantTPForSV = null; // Could be inherited or uninherited, influences SV_Multiplier

        // Find the relevant timing points active at or before the slider's hitTime
        for (TimingPoint tp : timingPoints) {
            if (tp.getTime() > this.getHitTime()) {
                break; // Timing points are sorted, no need to check further
            }

            if (!tp.isInherited()) {
                activeUninheritedTP = tp;
                lastRelevantTPForSV = tp; // An uninherited point resets SV multiplier from previous green lines
            } else { // tp is inherited
                // An inherited point is only relevant if it's after the current activeUninheritedTP
                // or if no uninheritedTP has been found yet (shouldn't happen in valid maps for sliders after time 0)
                if (activeUninheritedTP == null || tp.getTime() >= activeUninheritedTP.getTime()) {
                    lastRelevantTPForSV = tp;
                }
            }
        }

        double msPerBeat;
        if (activeUninheritedTP != null) {
            msPerBeat = activeUninheritedTP.getBeatLength();
        } else {
            // Fallback: No uninherited timing point found before the slider.
            // This is unusual for a slider not at the very beginning of the map.
            // Use a default beat duration (e.g., 120 BPM = 500ms/beat).
            msPerBeat = 500.0;
            // System.err.println("Warning: No uninherited timing point found for slider at " + getHitTime() + ". Using default beatLength.");
        }

        double svMultiplierFromTimingPoint = 1.0; // Default: no SV modification from green lines
        if (lastRelevantTPForSV != null && lastRelevantTPForSV.isInherited()) {
            // The beatLength of an inherited point is a negative percentage.
            // e.g., -50 means 0.5x speed. Slider velocity is multiplied by (-100 / value).
            // If value = -50 (0.5x speed), SV effect = -100 / -50 = 2.0 (slider moves 2x faster relative to beats).
            // If value = -200 (2x speed), SV effect = -100 / -200 = 0.5 (slider moves 0.5x slower relative to beats).
            if (lastRelevantTPForSV.getBeatLength() != 0) {
                svMultiplierFromTimingPoint = -100.0 / lastRelevantTPForSV.getBeatLength();
            } else {
                // svMultiplierFromTimingPoint = Double.POSITIVE_INFINITY; // Avoid division by zero, effectively making duration near zero
                // Or treat as 1.0? Osu seems to treat 0 as 1x.
                svMultiplierFromTimingPoint = 1.0;
                System.err.println("Warning: Inherited timing point with 0 beatLength at " + lastRelevantTPForSV.getTime());
            }
        }

        // osu!pixels per beat = BaseSliderVelocity (which is baseSliderMultiplier * 100 osu!pixels/beat) * svMultiplierFromTimingPoint
        double effectivePixelsPerBeat = baseSliderMultiplier * 100.0 * svMultiplierFromTimingPoint;

        if (pixelLength == 0) return 0; // No length, no duration
        if (effectivePixelsPerBeat == 0) {
            // System.err.println("Warning: Effective pixels per beat is zero for slider at " + getHitTime() + ". Duration will be infinite.");
            return Double.POSITIVE_INFINITY; // Avoid division by zero
        }

        // Duration = (Total Pixels / (Pixels / Beat)) * (Milliseconds / Beat)
        double singlePassDuration = (pixelLength / effectivePixelsPerBeat) * msPerBeat;
        return singlePassDuration;
    }


    private void parseSliderParams(String paramsStr, int startX, int startY) {
        String[] mainParts = paramsStr.split(",");

        String curveData = mainParts[0];
        String[] curveParts = curveData.split("\\|");
        if (curveParts.length > 0 && !curveParts[0].isEmpty()) {
            this.sliderType = curveParts[0].charAt(0);
        } else {
            this.sliderType = 'L'; // Default if empty
        }

        this.controlPoints.clear();
        this.controlPoints.add(new Point2D(startX, startY));

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
        // Ensure there's at least one segment if it's a slider
        if (controlPoints.size() < 2 && this.sliderType != '?') {
            System.err.println("Warning: Slider has less than 2 control points. Adding a dummy endpoint. Params: " + paramsStr);
            this.controlPoints.add(new Point2D(startX + 100, startY)); // Default offset if only start point given
        }


        if(!mainParts[1].isEmpty()) {
            try {
                this.repeats = Integer.parseInt(mainParts[1]);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing slider repeats: " + mainParts[1] + ". Defaulting to 1.");
                this.repeats = 1;
            }
        } else {
            this.repeats = 1; // Default if empty
        }

        if(mainParts.length > 2 && !mainParts[2].isEmpty()){
            try {
                this.pixelLength = Double.parseDouble(mainParts[2]);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing slider pixelLength: " + mainParts[2] + ". Defaulting to 0.");
                this.pixelLength = 0.0;
            }
        } else {
            this.pixelLength = 0.0; // Default if not present or empty
        }

        if (mainParts.length > 3) {
            this.edgeSoundsStr = mainParts[3];
        }
        if (mainParts.length > 4) {
            this.edgeSetsStr = mainParts[4];
        }
    }

    public HitSlider(int osuX, int osuY, long hitTime, int type, int hitSound,
                     String objectParams, String hitSample, double approachRate,
                     double circleSize, double sliderMultiplier) {
        super(osuX, osuY, hitTime, type, hitSound, hitSample, approachRate, circleSize);
        PATH_STROKE_WIDTH = getCircleRadius() * 2;
        BALL_RADIUS = getCircleRadius() * 0.8;

        parseSliderParams(objectParams, getOsuX(), getOsuY());

        this.duration = calculateSliderDuration(sliderMultiplier, OsuParser.getTimingPointsList());
        if (Double.isInfinite(this.duration) || Double.isNaN(this.duration) || this.duration <= 0) {
            System.err.println("Warning: Invalid slider duration calculated (" + this.duration + ") for slider at " + getHitTime() + ". Setting to a fallback value.");
            this.duration = 500; // Fallback duration if calculation fails
        }

        this.endTime = getHitTime() + (long) (this.duration * this.repeats);

        group = new Group();
        group.setVisible(false);

        sliderPath = createSliderPath();
        if (sliderPath != null) {
            group.getChildren().add(sliderPath);
        }

        headCircle = new Circle(0, 0, getCircleRadius());
        headCircle.setFill(Color.rgb(100, 180, 255, 0.8));
        headCircle.setStroke(Color.WHITE);
        headCircle.setStrokeWidth(2);
        group.getChildren().add(headCircle);

        approachCircle = new Circle(0, 0, getCircleRadius());
        approachCircle.setFill(Color.TRANSPARENT);
        approachCircle.setStroke(Color.WHITE);
        approachCircle.setStrokeWidth(APPROACH_STROKE_WIDTH);
        approachCircle.setScaleX(APPROACH_START_SCALE);
        approachCircle.setScaleY(APPROACH_START_SCALE);
        group.getChildren().add(approachCircle);

        sliderBall = new Circle(0, 0, BALL_RADIUS);
        sliderBall.setFill(Color.WHITE.deriveColor(1, 1, 1, 0.7));
        sliderBall.setVisible(false);
        group.getChildren().add(sliderBall);

        group.setUserData(this);
        handleEvent();
    }

    private Path createSliderPath() {
        if (controlPoints.isEmpty()) {
            System.err.println("Error: Cannot create slider path, control points list is empty for slider at " + getHitTime());
            return null;
        }
        if (controlPoints.size() < 2 && sliderType != '?') { // Allow '?' (unknown/parsed error) to potentially skip path gen
            System.err.println("Error: Slider path needs at least 2 control points. Slider at " + getHitTime());
            // Create a minimal path to avoid NPE, though it won't be correct
            Path dummyPath = new Path(new MoveTo(0,0), new LineTo(1,0)); // Minimal path
            dummyPath.setStroke(Color.RED); // Indicate error
            dummyPath.setStrokeWidth(PATH_STROKE_WIDTH);
            return dummyPath;
        }

        Path path = new Path();
        path.setStroke(Color.BLUE); // TODO: Use colors based on combo
        path.setStrokeWidth(PATH_STROKE_WIDTH);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);

        Point2D start = controlPoints.get(0);
        path.getElements().add(new MoveTo(0, 0)); // Path is relative to the group's origin (slider head's center)

        // TODO: Implement Bezier and Perfect Circle paths based on sliderType
        // For now, only linear segments:
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
        if (approachCircle == null || headHit) return;

        approachCircle.setVisible(true);
        approachCircle.setScaleX(APPROACH_START_SCALE);
        approachCircle.setScaleY(APPROACH_START_SCALE);

        approachAnimation = new ScaleTransition(Duration.millis(getPreempt()), approachCircle);
        approachAnimation.setFromX(APPROACH_START_SCALE);
        approachAnimation.setFromY(APPROACH_START_SCALE);
        approachAnimation.setToX(1.0);
        approachAnimation.setToY(1.0);
        approachAnimation.play();
    }

    private void hide() {
        group.setVisible(false);
        setVisible(false); // Also update HitObject's visibility
        if(group.getParent() instanceof Pane) {
            ((Pane) group.getParent()).getChildren().remove(group);
        }
    }

    private double getBallFraction(double timeSinceHitStart) {
        // `this.duration` is for a single pass.
        // `this.repeats` is the number of times it repeats *after* the first pass.
        int totalTraversals = this.repeats + 1;
        if (totalTraversals <= 0) totalTraversals = 1; // Should not happen with repeats >= 0

        double timeForOneTraversal = this.duration;
        if (timeForOneTraversal <= 0) timeForOneTraversal = 1; // Avoid division by zero if duration is 0

        // Which traversal are we in (0 for first, 1 for first repeat, etc.)
        int currentTraversalIndex = (int) Math.floor(timeSinceHitStart / timeForOneTraversal);

        if (currentTraversalIndex >= totalTraversals) {
            // We are past the end of all traversals, ball should be at the end of the last traversal
            return (totalTraversals % 2 == 1) ? 1.0 : 0.0; // If odd traversals, end at 1.0; if even, end at 0.0
        }

        // Fraction of time elapsed within the current traversal
        double timeIntoCurrentTraversal = timeSinceHitStart - (currentTraversalIndex * timeForOneTraversal);
        double fractionInCurrentTraversal = timeIntoCurrentTraversal / timeForOneTraversal;

        boolean isReverse = (currentTraversalIndex % 2) != 0; // 0th pass (initial) is forward, 1st pass (first repeat) is reverse

        double ballFraction = isReverse ? (1.0 - fractionInCurrentTraversal) : fractionInCurrentTraversal;
        return Math.max(0.0, Math.min(1.0, ballFraction)); // Clamp to [0, 1]
    }


    private Point2D getPointOnLinear(Point2D p0, Point2D p1, double t) {
        t = Math.max(0.0, Math.min(1.0, t)); // Clamp t to [0,1]
        double x = (1.0 - t) * p0.getX() + t * p1.getX();
        double y = (1.0 - t) * p0.getY() + t * p1.getY();
        return new Point2D(x, y);
    }

    private Point2D getVisualPointAtFraction(double fraction) {
        if (controlPoints.size() < 2) {
            // System.err.println("Warning: Not enough control points to determine visual point. Slider at " + getHitTime());
            return new Point2D(0, 0); // No path or malformed
        }

        Point2D sliderStartAbs = controlPoints.get(0); // Absolute start coordinate of the slider

        // TODO: This needs to handle different slider types (Bezier, Perfect Circle, Linear)
        // Current implementation is for multi-segment linear paths.

        // Calculate total length of the path segments defined by controlPoints
        double totalVisualLength = 0;
        double[] segmentLengths = new double[controlPoints.size() - 1];
        if (controlPoints.size() -1 < 0) return new Point2D(0,0); // Should be caught by size < 2 check

        for (int i = 0; i < controlPoints.size() - 1; i++) {
            segmentLengths[i] = controlPoints.get(i).distance(controlPoints.get(i + 1));
            totalVisualLength += segmentLengths[i];
        }

        if (totalVisualLength == 0 && controlPoints.size() >= 2) {
            // All control points are coincident, or only one segment of zero length
            // Return the start point relative to the group.
            return controlPoints.get(0).subtract(sliderStartAbs); // which is (0,0)
        }
        if (totalVisualLength == 0) { // still zero after check, likely single point
            return new Point2D(0,0);
        }


        // Find which segment the fraction falls on
        double targetDistanceOnPath = fraction * totalVisualLength;
        double distanceAccumulated = 0;
        int segmentIndex = 0;

        for (int i = 0; i < segmentLengths.length; i++) {
            if (distanceAccumulated + segmentLengths[i] >= targetDistanceOnPath) {
                segmentIndex = i;
                break;
            }
            distanceAccumulated += segmentLengths[i];
            // If it's the last segment and we haven't broken, this must be it
            if (i == segmentLengths.length -1) segmentIndex = i;
        }

        double fractionWithinSegment;
        if (segmentLengths[segmentIndex] == 0) { // Avoid division by zero for zero-length segment
            fractionWithinSegment = 0; // or 1, depending on desired behavior for coincident points
        } else {
            fractionWithinSegment = (targetDistanceOnPath - distanceAccumulated) / segmentLengths[segmentIndex];
        }
        fractionWithinSegment = Math.max(0.0, Math.min(1.0, fractionWithinSegment)); // Clamp


        // Interpolate within the segment
        Point2D p0 = controlPoints.get(segmentIndex);
        Point2D p1 = controlPoints.get(segmentIndex + 1);

        Point2D interpolatedAbsolutePoint = getPointOnLinear(p0, p1, fractionWithinSegment);

        // Return the point relative to the slider's group origin (which is sliderStartAbs)
        return interpolatedAbsolutePoint.subtract(sliderStartAbs);
    }


    @Override
    public Node getNode() {
        return group;
    }

    @Override
    public void update(long currentTime) {
        setCurrTime(currentTime);
        long timeUntilHit = getHitTime() - getCurrTime();
        long timeSinceHitStart = getCurrTime() - getHitTime();

        if (!isVisible() && timeUntilHit <= getPreempt()) {
            appear();
        }

        if (headHit) {
            if (getCurrTime() <= endTime) { // Ball is moving
                sliderBall.setVisible(true); // Ensure visible if head was hit
                double ballFraction = getBallFraction(timeSinceHitStart);
                Point2D ballPos = getVisualPointAtFraction(ballFraction);
                sliderBall.setCenterX(ballPos.getX());
                sliderBall.setCenterY(ballPos.getY());
            } else { // Slider finished
                if (isVisible()) hide(); // Hide only if it was visible
            }
        }

        // Miss logic
        // If head wasn't hit and current time is past the hit window for the head
        if (isVisible() && !headHit && timeSinceHitStart > getHitWindowGreat()) { // Using a typical "great" window as miss threshold for head
            // System.out.println("Slider Head Missed (Timeout): " + getOsuX() + "," + getOsuY() + " at " + currentTime + "ms");
            hide();
        } else if (isVisible() && headHit && getCurrTime() > endTime + 200) { // If holding past end time
            // If slider was hit, it should hide itself when endTime is reached.
            // This is an extra failsafe if it's still visible after its supposed end.
            hide();
        }
    }

    @Override
    public void handleEvent() {
        group.setOnMousePressed(e -> { // Changed to group, OnMousePressed might feel more responsive for holds
            if (isVisible() && !headHit && e.getTarget() != sliderBall) {
                long clickTime = getCurrTime(); // Use the game's current time
                long timingError = Math.abs(clickTime - getHitTime()); // Time difference from perfect head hit

                // Check if click is within hit window for the head
                // Assuming getHitWindowGreat() gives a reasonable timing window (e.g. ~80-150ms depending on OD)
                if (timingError <= getHitWindowGreat()) {
                    Point2D clickInGroup = new Point2D(e.getX(), e.getY());
                    // Check if click is on or near the head circle
                    if (clickInGroup.distance(0,0) <= getCircleRadius() * 1.5) { // Generous click area for head
//                        System.out.println("Slider Head Hit: " + getOsuX() + "," + getOsuY() + " | Timing: " + (clickTime - getHitTime()) + "ms");
                        headHit = true;
                        setHit(true); // Mark the HitObject as hit

                        if (approachAnimation != null) approachAnimation.stop();
                        approachCircle.setVisible(false);
                        headCircle.setVisible(false); // Hide static head
                        sliderBall.setVisible(true);  // Show the moving ball immediately at start

                        // Initialize ball position at fraction 0
                        Point2D initialBallPos = getVisualPointAtFraction(0.0);
                        sliderBall.setCenterX(initialBallPos.getX());
                        sliderBall.setCenterY(initialBallPos.getY());

                        // TODO: Play hitsound for slider head
                    } else {
                        // Clicked at the right time, but missed the head spatially
                        // System.out.println("Slider Head Miss (Position): Dist: " + clickInGroup.distance(0,0));
                        // hide(); // Optionally miss if clicked outside head area even if timing is right
                    }
                } else {
                    // Clicked too early or too late for the head
                    // System.out.println("Slider Head Miss (Timing): " + (clickTime - getHitTime()) + "ms");
                    // If clicked too early before preempt, don't hide. If clicked way too late, it's a miss.
                    if (clickTime > getHitTime() + getHitWindowMeh()){ // If clicked well past the hittable window
                        hide();
                    }
                }
            }
        });

        // Optional: Handle mouse release if you want to penalize releasing the slider early
        // group.setOnMouseReleased(e -> {
        //     if (headHit && isVisible()) {
        //         // Check if slider was released before endTime
        //         if (getCurrTime() < endTime - some_leniency) {
        //             System.out.println("Slider broken!");
        //             // Handle slider break (e.g., reduce score, change visual feedback)
        //             // For simplicity, we can just let it complete, or hide it.
        //             hide();
        //         }
        //     }
        // });
    }

    @Override
    public void setPosition(double paneX, double paneY) {
        if(group != null) {
            // The group's origin (0,0) should be the center of the head circle.
            // The path and other elements are positioned relative to this.
            group.relocate(paneX - getCircleRadius(), paneY - getCircleRadius());
        }
    }

    @Override
    public void updateVisuals(double centerX, double centerY, double scaledRadius) {
        if(group != null) {
//            group.relocate(centerX - getCircleRadius(), centerY - getCircleRadius());

            group.setLayoutX(centerX);
            group.setLayoutY(centerY);

            // Update the radius of the circles based on the scaleFactor
            headCircle.setRadius(scaledRadius);
            approachCircle.setRadius(scaledRadius);
            sliderPath.setStrokeWidth(scaledRadius * 2);
            sliderBall.setRadius(scaledRadius * 0.8);
        }
    }

    // Dummy methods for hit windows, replace with actual calculation based on OverallDifficulty
    private long getHitWindowGreat() { return 150; } // Example
    private long getHitWindowMeh() { return 250; } // Example
}