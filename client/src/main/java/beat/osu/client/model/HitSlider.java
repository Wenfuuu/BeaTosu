package beat.osu.client.model;

import beat.osu.client.Main;
import beat.osu.client.enums.HitResult;
import beat.osu.client.factory.HitObjectFactory;
import beat.osu.client.helper.SfxManager;
import beat.osu.client.interfaces.game.HitObjectListener;
import beat.osu.client.utils.OsuParser;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class HitSlider extends HitObject {
    private final HitObjectListener listener;

    private final Group headGroup;
    private final Circle headCircle;
    private final Path sliderPath;
    private final Path borderPath;
    private final Circle sliderBall;
    private final Circle sliderBallOuter;
    private final Circle approachCircle;
    private final Label comboLabel;

    // Parsed Slider Data
    private char sliderType = '?';
    private final List<Point2D> controlPoints = new ArrayList<>();
    private int slides = 1;
    private double pixelLength = 0.0;
    private String edgeSoundsStr = "";
    private String edgeSetsStr = "";
    private ArrayList<ArrayList<String>> edfeSfxFilenames;
    private final List<Circle> sliderTicks = new ArrayList<>();

    // Timing & Animation
    private final long endTime;

    private double duration; // Duration of a SINGLE traversal of the slider in milliseconds
    private double sliderVelocity;
    private double msPerBeat;
    private double sliderTickRate;
    private int tickCount = 0;
    private int currentTickIndex = -1;

    // Slider scoring tracking
    @Setter
    private boolean earlyHit = false;
    @Getter
    private boolean tailMissed = false;
    private int ticksHit = 0;
    private int repeatsHit = 0;
    private final List<Boolean> tickHitStatus = new ArrayList<>();
    private final List<Boolean> repeatHitStatus = new ArrayList<>();
    private boolean headHit = false;
    private boolean mouseInBallRadius = false;
    private boolean keyHolded = false;
    // private List<MediaPlayer> activePlayers = new ArrayList<>();
    private ParallelTransition parallelAnimation;
    private final List<ImageView> reverseArrows = new ArrayList<>();
    private int currentTraversalIndex = -1; // Visual Constants
    private final double PATH_STROKE_WIDTH;
    private final double BALL_RADIUS;
    private final double BALL_OUTER_RADIUS;
    private final double TICK_RADIUS;

    private int calculateTickCount(double tickRate) {
        if (tickRate <= 0 || this.msPerBeat <= 0 || this.duration <= 0) {
            return 0;
        }

        // Calculate tick interval in milliseconds
        double tickInterval = msPerBeat / tickRate;

        // Calculate how many ticks appear in one traversal (excluding head and tail)
        int ticksPerTraversal = (int) Math.floor(this.duration / tickInterval);

        // Total ticks = ticks per traversal * total number of traversals
        int totalTraversals = this.slides + 1;
        return ticksPerTraversal * totalTraversals;
    }

    private void calculateSliderDuration(double baseSliderMultiplier, ArrayList<TimingPoint> timingPoints) {
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
                // An inherited point is only relevant if it's after the current
                // activeUninheritedTP
                // or if no uninheritedTP has been found yet (shouldn't happen in valid maps for
                // sliders after time 0)
                if (activeUninheritedTP == null || tp.getTime() >= activeUninheritedTP.getTime()) {
                    lastRelevantTPForSV = tp;
                }
            }
        }

        double msBeat;
        if (activeUninheritedTP != null) {
            msBeat = activeUninheritedTP.getBeatLength();
        } else {
            // Fallback: No uninherited timing point found before the slider.
            // This is unusual for a slider not at the very beginning of the map.
            // Use a default beat duration (e.g., 120 BPM = 500ms/beat).
            int bpm = OsuParser.getBPM();
            msBeat = 60000.0 / bpm;
            System.out.println("falling back to default beat length of " + msBeat
                    + "ms for slider at " + getHitTime() + ". No uninherited timing point found.");
        }

        this.msPerBeat = msBeat;
        double svMultiplierFromTimingPoint = 1.0; // Default: no SV modification from green lines
        if (lastRelevantTPForSV != null && lastRelevantTPForSV.isInherited()) {
            // The beatLength of an inherited point is a negative percentage.
            // e.g., -50 means 0.5x speed. Slider velocity is multiplied by (-100 / value).
            // If value = -50 (0.5x speed), SV effect = -100 / -50 = 2.0 (slider moves 2x
            // faster relative to beats).
            // If value = -200 (2x speed), SV effect = -100 / -200 = 0.5 (slider moves 0.5x
            // slower relative to beats).
            if (lastRelevantTPForSV.getBeatLength() != 0) {
                svMultiplierFromTimingPoint = -100.0 / lastRelevantTPForSV.getBeatLength();
            } else {
                // svMultiplierFromTimingPoint = Double.POSITIVE_INFINITY; // Avoid division by
                // zero, effectively making duration near zero
                // Or treat as 1.0? Osu seems to treat 0 as 1x.
                System.err.println(
                        "Warning: Inherited timing point with 0 beatLength at " + lastRelevantTPForSV.getTime());
            }
        }

        // osu!pixels per beat = BaseSliderVelocity (which is baseSliderMultiplier * 100
        // osu!pixels/beat) * svMultiplierFromTimingPoint
        this.sliderVelocity = baseSliderMultiplier * 100.0 * svMultiplierFromTimingPoint;

        if (pixelLength == 0)
            this.duration = 0; // No length, no duration
        else if (sliderVelocity == 0) {
            // System.err.println("Warning: Effective pixels per beat is zero for slider at
            // " + getHitTime() + ". Duration will be infinite.");
            this.duration = Double.POSITIVE_INFINITY; // Avoid division by zero
        } else {
            this.duration = (pixelLength / sliderVelocity) * msBeat;
        }
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
                        System.err.println("Error parsing slider control point coordinates: " + curveParts[i] + " in "
                                + paramsStr);
                    }
                } else {
                    System.err.println("Warning: Invalid control point format (expected x:y): " + curveParts[i] + " in "
                            + paramsStr);
                }
            }
        }
        // Ensure there's at least one segment if it's a slider
        if (controlPoints.size() < 2 && this.sliderType != '?') {
            System.err.println(
                    "Warning: Slider has less than 2 control points. Adding a dummy endpoint. Params: " + paramsStr);
            this.controlPoints.add(new Point2D(startX + 100, startY)); // Default offset if only start point given
        }

        if (!mainParts[1].isEmpty()) {
            try {
                this.slides = Integer.parseInt(mainParts[1]);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing slider repeats: " + mainParts[1] + ". Defaulting to 1.");
                this.slides = 1;
            }
        } else {
            this.slides = 1; // Default if empty
        }

        if (mainParts.length > 2 && !mainParts[2].isEmpty()) {
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

        if (this.edgeSoundsStr.isEmpty()) {
            this.edgeSoundsStr = "0|".repeat(this.slides);
            this.edgeSoundsStr = this.edgeSoundsStr.substring(0, this.edgeSoundsStr.length() - 1);
        }
        if (this.edgeSetsStr.isEmpty()) {
            this.edgeSetsStr = "0:0|".repeat(this.slides);
            this.edgeSetsStr = this.edgeSetsStr.substring(0, this.edgeSetsStr.length() - 1);
        }
    }

    public HitSlider(int osuX, int osuY, long hitTime, int type, int hitSound,
            String objectParams, String hitSample, double approachRate,
            double circleSize, double sliderMultiplier, double sliderTickRate,
            int comboNumber, int comboSetIndex, String colorString,
            boolean comboEnd, ArrayList<String> sfxFilenames,
            HitObjectListener listener) {
        super(osuX, osuY, hitTime, type, hitSound, hitSample, approachRate,
                circleSize, comboNumber, comboSetIndex, comboEnd, sfxFilenames);
        PATH_STROKE_WIDTH = getCircleRadius() * 2;
        BALL_RADIUS = getCircleRadius() * 0.8;
        BALL_OUTER_RADIUS = getCircleRadius() * 1.5; // Larger radius for mouse detection
        TICK_RADIUS = getCircleRadius() * 0.15;

        parseSliderParams(objectParams, getOsuX(), getOsuY());
        this.edfeSfxFilenames = HitObjectFactory.generateSliderEdgeSfxFilenames(edgeSoundsStr, edgeSetsStr,
                (int) getHitTime());
        this.listener = listener;
        this.sliderTickRate = sliderTickRate; // Store tick rate for later use

        calculateSliderDuration(sliderMultiplier, OsuParser.getTimingPointsList());
        if (Double.isInfinite(this.duration) || Double.isNaN(this.duration) || this.duration <= 0) {
            System.err.println("Warning: Invalid slider duration calculated (" + this.duration + ") for slider at "
                    + getHitTime() + ". Setting to a fallback value.");
            this.duration = 500; // Fallback duration if calculation fails
        }

        this.tickCount = calculateTickCount(sliderTickRate);
        this.endTime = getHitTime() + (long) (this.duration * this.slides);

        // Initialize slider scoring tracking
        for (int i = 0; i < this.tickCount; i++) {
            tickHitStatus.add(false);
        }
        for (int i = 0; i < this.slides; i++) {
            repeatHitStatus.add(false);
        }

        // get colors
        Color circleColor = parseColorString(colorString);
        sliderPath = createSliderPath();
        borderPath = createSliderPath();
        if (sliderPath != null && borderPath != null) {
            borderPath.setStroke(Color.rgb(168, 107, 121, 0.8));
            // sliderPath.setStroke(circleColor.deriveColor(1, 1, 1, 0.8));
            sliderPath.setStroke(Color.rgb(0, 0, 0, 0.5));

            group.getChildren().addAll(borderPath, sliderPath);
        }

        headCircle = new Circle(0, 0, getCircleRadius());
        headCircle.setFill(circleColor.deriveColor(1, 1, 1, 0.3));
        headCircle.setStroke(Color.WHITE);
        headCircle.setStrokeWidth(CIRCLE_STROKE_WIDTH);

        comboLabel = new Label(String.valueOf(getComboNumber()));
        comboLabel.setFont(Font.font("Aller", 50));
        comboLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        comboLabel.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            comboLabel.setLayoutX(-newBounds.getWidth() / 2);
            comboLabel.setLayoutY(-newBounds.getHeight() / 2);
        });

        headGroup = new Group(headCircle, comboLabel);

        group.getChildren().add(headGroup);

        approachCircle = new Circle(0, 0, getCircleRadius());
        approachCircle.setFill(Color.TRANSPARENT);
        approachCircle.setStroke(circleColor.deriveColor(1, 1, 1, 0.8));
        approachCircle.setStrokeWidth(CIRCLE_STROKE_WIDTH);
        approachCircle.setScaleX(APPROACH_START_SCALE);
        approachCircle.setScaleY(APPROACH_START_SCALE);
        group.getChildren().add(approachCircle);
        sliderBall = new Circle(0, 0, BALL_RADIUS);
        sliderBall.setFill(Color.WHITE.deriveColor(1, 1, 1, 0.7));
        sliderBall.setVisible(false);
        group.getChildren().add(sliderBall);

        sliderBallOuter = new Circle(0, 0, BALL_OUTER_RADIUS);
        sliderBallOuter.setFill(Color.WHITE.deriveColor(1, 1, 1, 0.35));
        sliderBallOuter.setStroke(Color.WHITE);
        sliderBallOuter.setVisible(false);
        group.getChildren().add(sliderBallOuter);

        createReverseArrows();
        createSliderTicks(sliderTickRate);

        group.setUserData(this);
    }

    private void createSliderTicks(double tickRate) {
        for (Circle tick : sliderTicks) {
            group.getChildren().remove(tick);
        }
        sliderTicks.clear();
        if (tickRate <= 0 || this.duration <= 0) {
            return; // No ticks to create
        }

        double minPixelDistanceFromEnds = getCircleRadius();

        double tickSpacing = msPerBeat / tickRate;
        for (int repeat = 0; repeat < this.slides; repeat++) {
            // Calculate how many ticks in this span
            int ticksInSpan = (int) Math.floor((this.duration - 1e-3) / tickSpacing);

            for (int tickIndex = 0; tickIndex < ticksInSpan; tickIndex++) {
                double tickTimeInSpan = (tickIndex + 1) * tickSpacing; // +1 because first tick is after the start

                if (tickTimeInSpan >= this.duration - 1e-3) {
                    break; // Don't place tick too close to the end
                }

                // Calculate position along the slider
                double fractionAlongSlider = tickTimeInSpan / this.duration;

                // For reverse spans (odd repeat indices), reverse the fraction
                if (repeat % 2 == 1) {
                    fractionAlongSlider = 1.0 - fractionAlongSlider;
                }

                // Pixel-based validation: check distance from head and tail
                Point2D headPos = getVisualPointAtFraction(0.0); // Slider head position
                Point2D tailPos = getVisualPointAtFraction(1.0); // Slider tail position
                Point2D tickPos = getVisualPointAtFraction(fractionAlongSlider);

                // Calculate pixel distances
                double distanceFromHead = headPos.distance(tickPos);
                double distanceFromTail = tailPos.distance(tickPos);

                // Skip this tick if it's too close to either end
                if (distanceFromHead < minPixelDistanceFromEnds ||
                        distanceFromTail < minPixelDistanceFromEnds) {
                    continue;
                }

                // Create the tick circle
                Circle tick = new Circle(tickPos.getX(), tickPos.getY(), TICK_RADIUS - 2);
                tick.setFill(Color.TRANSPARENT);
                tick.setStroke(Color.WHITE);
                tick.setStrokeWidth(5);

                sliderTicks.add(tick);
                group.getChildren().add(tick);
            }
        }
    }

    private void updateTickVisuals(double timeSinceHitStart) {
        if (!headHit || sliderTicks.isEmpty())
            return;

        if (!mouseInBallRadius || !keyHolded)
            return;

        double tickSpacing = msPerBeat / calculateTickRate();
//        int newTickIndex = (int) Math.floor(timeSinceHitStart / tickSpacing);

        // Check if we've passed new ticks and count them as hit
//        for (int i = currentTickIndex + 1; i <= newTickIndex && i < sliderTicks.size(); i++) {
//            if (i >= 0 && i < tickHitStatus.size() && !tickHitStatus.get(i)) {
//                tickHitStatus.set(i, true);
//                ticksHit++;
//                // add 10 score
//                listener.onSliderTick(this);
//                System.out.println("Tick " + i + " hit! Total ticks hit: " + ticksHit);
//            }
//        }

        currentTickIndex = (int) Math.floor(timeSinceHitStart / tickSpacing);;
        System.out.println("Current tick index: " + currentTickIndex + ", total ticks: " + sliderTicks.size());
        for (int i = 0; i < sliderTicks.size() && i < currentTickIndex; i++) {
            if (sliderTicks.get(i).isVisible()) {
                tickHitStatus.set(i, true);
                ticksHit++;
                // add 10 score
                listener.onSliderTick(this);
                SfxManager.playBeatmapSfx("soft-slidertick.wav");
                sliderTicks.get(i).setVisible(false);
            }
        }
    }

    private double calculateTickRate() {
        return this.sliderTickRate;
    }

    private void updateArrowVisibility(int currentTraversalIndex) {
        if (reverseArrows.isEmpty())
            return;

        // Determine which arrow should be visible
        // Traversal 0: going to end (show end arrow)
        // Traversal 1: going to start (show start arrow if it exists)
        // Traversal 2: going to end again (show end arrow)
        // etc.
        int totalTraversals = slides;
        if (currentTraversalIndex < totalTraversals - 1) { // Not the final traversal
            if (currentTraversalIndex % 2 == 0) {
                // Even traversal index: going towards end, show end arrow
                reverseArrows.get(0).setVisible(true);
                if (reverseArrows.size() >= 2) {
                    reverseArrows.get(1).setVisible(false); // Hide start arrow
                }
            } else {
                // Odd traversal index: going towards start, show start arrow
                if (reverseArrows.size() >= 2) {
                    reverseArrows.get(1).setVisible(true); // Start arrow
                    reverseArrows.get(0).setVisible(false); // Hide end arrow
                }
            }
        } else { // hide arrow for final traversal
            for (ImageView arrow : reverseArrows) {
                arrow.setVisible(false);
            }
        }
    }

    private void createReverseArrows() {
        if (slides < 2 || controlPoints.size() < 2)
            return;

        // Clear any existing arrows
        for (ImageView arrow : reverseArrows) {
            group.getChildren().remove(arrow);
        }
        reverseArrows.clear();

        Image arrowImage = new Image(Objects.requireNonNull(Main.class
                .getResource("/assets/images/gameplay/reversearrow.png")).toExternalForm());

        Point2D startPoint = controlPoints.get(0);
        Point2D endPoint = controlPoints.get(controlPoints.size() - 1);

        // Create arrow at the end of the slider (where it will reverse)
        ImageView endArrow = new ImageView(arrowImage);
        endArrow.setFitWidth(getCircleRadius() * 2.5);
        endArrow.setFitHeight(getCircleRadius() * 2.5);
        endArrow.setPreserveRatio(true);

        // Position at the end point (relative to slider start)
        Point2D endRelative = endPoint.subtract(startPoint);
        endArrow.setLayoutX(endRelative.getX() - endArrow.getFitWidth() / 2);
        endArrow.setLayoutY(endRelative.getY() - endArrow.getFitHeight() / 2);

        // Calculate rotation angle based on the direction of the last segment
        if (controlPoints.size() >= 2) {
            Point2D secondLast = controlPoints.get(controlPoints.size() - 2);
            Point2D last = controlPoints.get(controlPoints.size() - 1);

            double angle = Math.toDegrees(Math.atan2(
                    last.getY() - secondLast.getY(),
                    last.getX() - secondLast.getX()));
            endArrow.setRotate(angle + 180); // +180 to point back towards the slider
        }

        reverseArrows.add(endArrow);
        group.getChildren().add(endArrow);

        // If there are multiple repeats (odd number means it ends at start, even at
        // end)
        // Add arrow at start point for even number of total traversals
        if (slides > 2) {
            ImageView startArrow = new ImageView(arrowImage);
            startArrow.setFitWidth(getCircleRadius() * 2.5);
            startArrow.setFitHeight(getCircleRadius() * 2.5);
            startArrow.setPreserveRatio(true);

            // Position at start (0,0 relative to group)
            startArrow.setLayoutX(-startArrow.getFitWidth() / 2);
            startArrow.setLayoutY(-startArrow.getFitHeight() / 2);

            // Calculate rotation for start arrow
            if (controlPoints.size() >= 2) {
                Point2D first = controlPoints.get(0);
                Point2D second = controlPoints.get(1);

                double angle = Math.toDegrees(Math.atan2(
                        second.getY() - first.getY(),
                        second.getX() - first.getX()));
                startArrow.setRotate(angle + 180); // Point back along the slider
            }

            startArrow.setVisible(false);
            reverseArrows.add(startArrow);
            group.getChildren().add(startArrow);
        }
    }

    private Path createSliderPath() {
        if (controlPoints.isEmpty()) {
            System.err.println(
                    "Error: Cannot create slider path, control points list is empty for slider at " + getHitTime());
            return null;
        }
        if (controlPoints.size() < 2 && sliderType != '?') { // Allow '?' (unknown/parsed error) to potentially skip
                                                             // path gen
            System.err.println("Error: Slider path needs at least 2 control points. Slider at " + getHitTime());
            // Create a minimal path to avoid NPE, though it won't be correct
            Path dummyPath = new Path(new MoveTo(0, 0), new LineTo(1, 0)); // Minimal path
            dummyPath.setStroke(Color.RED); // Indicate error
            dummyPath.setStrokeWidth(PATH_STROKE_WIDTH);
            return dummyPath;
        }

        Path path = new Path();
        path.setStrokeWidth(PATH_STROKE_WIDTH);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);

        Point2D start = controlPoints.get(0);
        path.getElements().add(new MoveTo(0, 0)); // Path is relative to the group's origin (slider head's center)

        // linear segments:
        if (sliderType == 'L' || sliderType == 'C') {
            for (int i = 1; i < controlPoints.size(); i++) {
                Point2D p = controlPoints.get(i);
                path.getElements().add(new LineTo(p.getX() - start.getX(), p.getY() - start.getY()));
            }
        } else if (sliderType == 'P') { // Perfect Circle
            // 'P' must have exactly 3 control points: start, middle point on arc, and end
            if (controlPoints.size() == 3) {
                Point2D middle = controlPoints.get(1);
                Point2D end = controlPoints.get(2);
                Point2D center = calculateCircleCenter(start, middle, end);

                if (center != null) {
                    double radius = center.distance(start);

                    // Calculate relative positions
                    double endX = end.getX() - start.getX();
                    double endY = end.getY() - start.getY();

                    // Determine sweep flag: 1 = clockwise, 0 = counter-clockwise
                    // Vector from center to start and end
                    Point2D vStart = start.subtract(center);
                    Point2D vEnd = end.subtract(center);
                    double cross = vStart.getX() * vEnd.getY() - vStart.getY() * vEnd.getX();
                    boolean sweepFlag = cross > 0; // Clockwise if negative (JavaFX sweepFlag=true)

                    ArcTo arcTo = new ArcTo(radius, radius, 0,
                            endX, endY, false, sweepFlag);
                    path.getElements().add(arcTo);
                } else {
                    // Fallback to linear if center calculation fails
                    for (int i = 1; i < controlPoints.size(); i++) {
                        Point2D p = controlPoints.get(i);
                        path.getElements().add(new LineTo(p.getX() - start.getX(), p.getY() - start.getY()));
                    }
                }
            } else {
                System.err.println("Warning: 'P' slider type requires exactly 3 control points (start, middle, end). "
                        + "Using linear fallback for slider at " + getHitTime() + ", control points: "
                        + controlPoints.size());
                for (int i = 1; i < controlPoints.size(); i++) {
                    Point2D p = controlPoints.get(i);
                    path.getElements().add(new LineTo(p.getX() - start.getX(), p.getY() - start.getY()));
                }
            }
        } else if (sliderType == 'B') {
            List<Point2D> currentSegment = new ArrayList<>();
            currentSegment.add(controlPoints.get(0)); // Start with the first point

            for (int i = 1; i < controlPoints.size(); ++i) {
                Point2D currentPoint = controlPoints.get(i);
                currentSegment.add(currentPoint);

                boolean isAnchor = i < controlPoints.size() - 1 && currentPoint.equals(controlPoints.get(i + 1));
                boolean isLastPoint = i == controlPoints.size() - 1;

                if (isAnchor || isLastPoint) {
                    addBezierSegmentToPath(path, currentSegment, start);
                    if (isAnchor) {
                        currentSegment.clear();
                        currentSegment.add(currentPoint);
                    }
                }
            }
        } else { // fallback to linear if unknown type
            for (int i = 1; i < controlPoints.size(); i++) {
                Point2D p = controlPoints.get(i);
                path.getElements().add(new LineTo(p.getX() - start.getX(), p.getY() - start.getY()));
            }
        }

        return path;
    }

    @Override
    public void pauseAnimations() {
        if (parallelAnimation != null && parallelAnimation.getStatus() == Animation.Status.RUNNING) {
            parallelAnimation.pause();
        }
    }

    @Override
    public void resumeAnimations() {
        if (parallelAnimation != null && parallelAnimation.getStatus() == Animation.Status.PAUSED) {
            parallelAnimation.play();
        }
    }

    private double getBallFraction(double timeSinceHitStart) {
        int totalTraversals = this.slides + 1;
        if (totalTraversals <= 0)
            totalTraversals = 1; // Should not happen with repeats >= 0

        double timeForOneTraversal = this.duration;
        if (timeForOneTraversal <= 0)
            timeForOneTraversal = 1; // Avoid division by zero if duration is 0

        // Which traversal are we in (0 for first, 1 for first repeat, etc.)
        int currentTraversalIndex = (int) Math.floor(timeSinceHitStart / timeForOneTraversal);

        if (currentTraversalIndex >= totalTraversals) {
            // We are past the end of all traversals, ball should be at the end of the last
            // traversal
            return (totalTraversals % 2 == 1) ? 1.0 : 0.0; // If odd traversals, end at 1.0; if even, end at 0.0
        }

        // Fraction of time elapsed within the current traversal
        double timeIntoCurrentTraversal = timeSinceHitStart - (currentTraversalIndex * timeForOneTraversal);
        double fractionInCurrentTraversal = timeIntoCurrentTraversal / timeForOneTraversal;

        boolean isReverse = (currentTraversalIndex % 2) != 0; // 0th pass (initial) is forward, 1st pass (first repeat)
                                                              // is reverse

        double ballFraction = isReverse ? (1.0 - fractionInCurrentTraversal) : fractionInCurrentTraversal;
        return Math.max(0.0, Math.min(1.0, ballFraction)); // Clamp to [0, 1]
    }

    // --- Helper for Multi-Segment Bezier Path Drawing ---
    private void addBezierSegmentToPath(Path path, List<Point2D> segmentPoints, Point2D sliderStartAbs) {
        if (segmentPoints.size() < 2)
            return; // Need at least start and end

        Point2D start = segmentPoints.get(0);
        Point2D end = segmentPoints.get(segmentPoints.size() - 1);
        Point2D relStart = new Point2D(start.getX() - sliderStartAbs.getX(), start.getY() - sliderStartAbs.getY());
        Point2D relEnd = new Point2D(end.getX() - sliderStartAbs.getX(), end.getY() - sliderStartAbs.getY());

        if (segmentPoints.size() == 2) { // Straight line segment
            path.getElements().add(new LineTo(relEnd.getX(), relEnd.getY()));
        } else if (segmentPoints.size() == 3) { // Treat as simple cubic (like 'P')
            Point2D ctrl1 = segmentPoints.get(1);
            Point2D relCtrl1 = new Point2D(ctrl1.getX() - sliderStartAbs.getX(), ctrl1.getY() - sliderStartAbs.getY());
            path.getElements().add(new CubicCurveTo(relCtrl1.getX(), relCtrl1.getY(),
                    relCtrl1.getX(), relCtrl1.getY(),
                    relEnd.getX(), relEnd.getY()));
        } else if (segmentPoints.size() == 4) { // Standard cubic Bezier
            Point2D ctrl1 = segmentPoints.get(1);
            Point2D ctrl2 = segmentPoints.get(2);
            Point2D relCtrl1 = new Point2D(ctrl1.getX() - sliderStartAbs.getX(), ctrl1.getY() - sliderStartAbs.getY());
            Point2D relCtrl2 = new Point2D(ctrl2.getX() - sliderStartAbs.getX(), ctrl2.getY() - sliderStartAbs.getY());
            path.getElements().add(new CubicCurveTo(relCtrl1.getX(), relCtrl1.getY(),
                    relCtrl2.getX(), relCtrl2.getY(),
                    relEnd.getX(), relEnd.getY()));
        } else {
            // Higher order Bezier curve - use De Casteljau's algorithm to approximate with
            // path segments
            int samples = Math.max(20, segmentPoints.size() * 4); // More samples for smoother curves

            for (int i = 1; i <= samples; i++) {
                double t = (double) i / samples;
                Point2D currentPoint = getPointOnHighOrderBezier(segmentPoints, t);
                Point2D relCurrentPoint = new Point2D(currentPoint.getX() - sliderStartAbs.getX(),
                        currentPoint.getY() - sliderStartAbs.getY());
                path.getElements().add(new LineTo(relCurrentPoint.getX(), relCurrentPoint.getY()));
            }
        }
    }

    private Point2D getPointOnCubicBezier(Point2D p0, Point2D p1, Point2D p2, Point2D p3, double t) {
        if (t < 0)
            t = 0;
        if (t > 1)
            t = 1;
        double u = 1.0 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;

        double x = uuu * p0.getX() + 3 * uu * t * p1.getX() + 3 * u * tt * p2.getX() + ttt * p3.getX();
        double y = uuu * p0.getY() + 3 * uu * t * p1.getY() + 3 * u * tt * p2.getY() + ttt * p3.getY();

        return new Point2D(x, y);
    }

    private Point2D getPointOnSimpleBezier(Point2D p0, Point2D p1, Point2D p2, double t) {
        return getPointOnCubicBezier(p0, p1, p1, p2, t);
    }

    private Point2D getPointOnLinear(Point2D p0, Point2D p1, double t) {
        t = Math.max(0.0, Math.min(1.0, t)); // Clamp t to [0,1]
        double x = (1.0 - t) * p0.getX() + t * p1.getX();
        double y = (1.0 - t) * p0.getY() + t * p1.getY();
        return new Point2D(x, y);
    }

    private Point2D getPointOnPerfectCircle(Point2D start, Point2D center, Point2D end, double t) {
        Point2D vStart = start.subtract(center);
        Point2D vEnd = end.subtract(center);

        double radius = vStart.magnitude();
        if (radius == 0)
            return center;

        double angleStart = Math.atan2(vStart.getY(), vStart.getX());
        double angleEnd = Math.atan2(vEnd.getY(), vEnd.getX());

        double angleDiff = angleEnd - angleStart;

        // Cross product to determine sweep direction
        double cross = vStart.getX() * vEnd.getY() - vStart.getY() * vEnd.getX();

        boolean sweepClockwise = cross < 0;

        // Now enforce the sweep direction to match ArcTo’s visual result (clockwise =
        // sweepFlag = true)
        if (sweepClockwise && angleDiff > 0) {
            angleDiff -= 2 * Math.PI;
        } else if (!sweepClockwise && angleDiff < 0) {
            angleDiff += 2 * Math.PI;
        }

        double angleAtT = angleStart + t * angleDiff;

        double x = center.getX() + radius * Math.cos(angleAtT);
        double y = center.getY() + radius * Math.sin(angleAtT);

        return new Point2D(x, y);
    }

    private Point2D getPointOnPerfectCircleReversed(Point2D start, Point2D center, Point2D end, double t) {
        // This is like getPointOnPerfectCircle but traverses the arc in the opposite
        // direction
        return getPointOnPerfectCircle(end, center, start, 1.0 - t);
    }

    private Point2D getVisualPointAtFraction(double fraction) {
        if (controlPoints.size() < 2)
            return new Point2D(0, 0); // No path

        Point2D sliderStartAbs = controlPoints.get(0); // Absolute start coordinate of the slider

        double totalVisualLength = 0;
        double[] segmentLengths = new double[controlPoints.size() - 1];

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
            return new Point2D(0, 0);
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
            if (i == segmentLengths.length - 1)
                segmentIndex = i;
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

        Point2D interpolatedAbsolutePoint;
        if (sliderType == 'L' || sliderType == 'C') { // Linear or Cubic
            interpolatedAbsolutePoint = getPointOnLinear(p0, p1, fractionWithinSegment);
        } else if (sliderType == 'P') { // Perfect Circle
            if (controlPoints.size() == 3) {
                // For perfect circle, we need to calculate the actual center from the 3 points
                Point2D start = controlPoints.get(0);
                Point2D middle = controlPoints.get(1);
                Point2D end = controlPoints.get(2);
                Point2D center = calculateCircleCenter(start, middle, end);
                if (center != null) {
                    // Check which direction gives us the middle point on the arc
                    Point2D testPoint = getPointOnPerfectCircle(start, center, end, 0.5);
                    double distToMiddle = testPoint.distance(middle);

                    // If test point is far from middle, try the opposite direction
                    if (distToMiddle > getCircleRadius() * 0.1) { // tolerance check
                        // Swap start and end to get the other arc
                        interpolatedAbsolutePoint = getPointOnPerfectCircleReversed(start, center, end, fraction);
                    } else {
                        interpolatedAbsolutePoint = getPointOnPerfectCircle(start, center, end, fraction);
                    }
                } else {
                    // Fallback to linear if center calculation fails
                    interpolatedAbsolutePoint = getPointOnLinear(p0, p1, fractionWithinSegment);
                }
            } else {
                System.err
                        .println("Warning: 'P' slider type requires exactly 3 control points. Using linear fallback.");
                interpolatedAbsolutePoint = getPointOnLinear(p0, p1, fractionWithinSegment);
            }
        } else if (sliderType == 'B') {
            interpolatedAbsolutePoint = getBezierPointAtFraction(fraction);
        } else { // Fallback to linear for unknown types
            interpolatedAbsolutePoint = getPointOnLinear(p0, p1, fractionWithinSegment);
        }

        // Return the point relative to the slider's group origin (which is
        // sliderStartAbs)
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
            if (getCurrTime() <= endTime) { // only move if past hit time
                if (getCurrTime() > getHitTime()) {
                    double ballFraction = getBallFraction((double) timeSinceHitStart);
                    Point2D ballPos = getVisualPointAtFraction(ballFraction);
                    sliderBall.setCenterX(ballPos.getX());
                    sliderBall.setCenterY(ballPos.getY());
                    // Update outer circle position to match slider ball
                    sliderBallOuter.setCenterX(ballPos.getX());
                    sliderBallOuter.setCenterY(ballPos.getY());
                    int traversalIndex = (int) Math.floor((double) timeSinceHitStart / this.duration);
                    if (traversalIndex != currentTraversalIndex) {
                        ArrayList<String> sfxFilenames = edfeSfxFilenames.get(traversalIndex);
                        for (String sfx : sfxFilenames) {
                            SfxManager.playBeatmapSfx(sfx);
                        }

                        // Track repeat/tail hits when traversal changes
                        if (currentTraversalIndex >= 0 && currentTraversalIndex < slides) {
                            trackRepeatHit(currentTraversalIndex);
                        }

                        currentTraversalIndex = traversalIndex;
                        updateArrowVisibility(currentTraversalIndex);
                        // add 30 score
                        listener.onSliderRepeat(this);
                    }

                    updateTickVisuals(timeSinceHitStart);
                }
            } else { // Slider finished
                if (slides > 0 && repeatsHit < slides) {
                    trackRepeatHit(slides - 1); // Track the final tail
                    if (repeatHitStatus.get(slides - 1)) {// if last is hit
                        ArrayList<String> sfxFilenames = edfeSfxFilenames.get(edfeSfxFilenames.size() - 1);
                        for (String sfx : sfxFilenames) {
                            SfxManager.playBeatmapSfx(sfx);
                        }
                        listener.onSliderEnd(this);
                    } else {
                        tailMissed = true;
                    }
                }

                setVisible(false);
                playMissEffect();
            }
        }
    }

    public void updateSlider(double mouseX, double mouseY, boolean keyHolded) {
        if (isHit()) {
            if (getCurrTime() <= endTime) {
                boolean previousMouseInBallRadius = mouseInBallRadius;
                mouseInBallRadius = isMouseInBallRadius(mouseX, mouseY);
                boolean previousKeyHolded = this.keyHolded;
                this.keyHolded = keyHolded;
                // System.out.println("Key holded: " + keyHolded);

                // Update colors if the mouse state changed
                if (previousMouseInBallRadius != mouseInBallRadius ||
                        previousKeyHolded != this.keyHolded) {
                    updateSliderBallColors();
                }
            } else if (getCurrTime() > endTime) {
                HitResult judgement = getSliderJudgement();
                System.out.println("Head early hit: " + earlyHit + ", Ticks hit: " + ticksHit + "/"
                        + sliderTicks.size() + ", Repeats hit: " + repeatsHit + "/" + slides);

                if (judgement != HitResult.MISS) {
                    listener.onHit(this, judgement);
                } else {
                    listener.onMiss(this);
                }
            }
        }
    }

    private void trackRepeatHit(int repeatIndex) {
        if (!mouseInBallRadius || !keyHolded)
            return;

        if (repeatIndex >= 0 && repeatIndex < repeatHitStatus.size() && !repeatHitStatus.get(repeatIndex)) {
            repeatHitStatus.set(repeatIndex, true);
            repeatsHit++;
            System.out.println("Repeat " + repeatIndex + " hit! Total repeats hit: " + repeatsHit);
        }
    }

    private double calculateSliderCompletion() {
        // Calculate total slider parts
        int totalParts = 1; // Slider head early hit
        totalParts += sliderTicks.size();
        totalParts += slides;

        if (totalParts == 0)
            return 0.0;

        // Calculate hit parts
        int hitParts = 0;

        // check if early hit
        if (!earlyHit) {
            hitParts++;
        }

        // Count ticks hit
        hitParts += ticksHit;

        // Count repeats/tail hit
        hitParts += repeatsHit;

        return (double) hitParts / (double) totalParts;
    }

    private HitResult getSliderJudgement() {
        double completion = calculateSliderCompletion();

        if (completion >= 1.0) {
            return HitResult.PERFECT; // 100% completion = GREAT
        } else if (completion >= 0.5) {
            return HitResult.GREAT; // 50%+ completion = OK
        } else if (completion > 0.0) {
            return HitResult.GOOD; // Any slider part hit = MEH
        } else {
            return HitResult.MISS; // 0% completion = MISS
        }
    }

    @Override
    public void playAppearAnimation() {
        if (approachCircle == null || headHit)
            return;

        approachCircle.setVisible(true);
        approachCircle.setScaleX(APPROACH_START_SCALE);
        approachCircle.setScaleY(APPROACH_START_SCALE);

        ScaleTransition approachAnimation = new ScaleTransition(Duration.millis(getPreempt()), approachCircle);
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
        headHit = true;
        if (parallelAnimation != null)
            parallelAnimation.stop();

        approachCircle.setVisible(false);
        headGroup.setVisible(false);
        sliderBall.setVisible(true);
        sliderBallOuter.setVisible(true);
        Point2D initialBallPos = getVisualPointAtFraction(0.0);
        sliderBall.setCenterX(initialBallPos.getX());
        sliderBall.setCenterY(initialBallPos.getY());
        // Set outer circle to initial position as well
        sliderBallOuter.setCenterX(initialBallPos.getX());
        sliderBallOuter.setCenterY(initialBallPos.getY());

        // Set initial slider ball colors
        updateSliderBallColors();

        // (Optional) Add fade effect for headCircle if you want
        FadeTransition fade = new FadeTransition(Duration.millis(150), headGroup);
        fade.setToValue(0);
        fade.play();

        System.out.println("Slider head hit successfully!");
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
            group.setLayoutX(centerX);
            group.setLayoutY(centerY); // Update the radius of the circles based on the scaleFactor
            headCircle.setRadius(scaledRadius);
            approachCircle.setRadius(scaledRadius);
            sliderPath.setStrokeWidth(scaledRadius * 2);
            borderPath.setStrokeWidth(scaledRadius * 2.1);
            sliderBall.setRadius(scaledRadius * 0.8);
            sliderBallOuter.setRadius(scaledRadius * 1.5); // Outer circle with larger radius

            double tickRadius = scaledRadius * 0.15;
            for (Circle tick : sliderTicks) {
                tick.setRadius(tickRadius);
            }
        }
    }

    private boolean isMouseInBallRadius(double mouseX, double mouseY) {
        if (!headHit || sliderBall == null) {
            return false;
        }
        // Get the current ball position in screen coordinates
        // The ball position is relative to the group, which is positioned at
        // getScreenCenterX/Y
        double ballScreenX = getScreenCenterX() + sliderBall.getCenterX();
        double ballScreenY = getScreenCenterY() + sliderBall.getCenterY();

        double dx = mouseX - ballScreenX;
        double dy = mouseY - ballScreenY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= (getScreenRadius() * 1.5); // Using the same multiplier as BALL_OUTER_RADIUS
    }

    private Point2D calculateCircleCenter(Point2D p1, Point2D p2, Point2D p3) {
        // Calculate the center of a circle from three points using the circumcenter
        // formula
        double ax = p1.getX(), ay = p1.getY();
        double bx = p2.getX(), by = p2.getY();
        double cx = p3.getX(), cy = p3.getY();

        double d = 2 * (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by));
        if (Math.abs(d) < 1e-10) {
            // Points are collinear or too close, cannot form a circle
            return null;
        }

        double ux = ((ax * ax + ay * ay) * (by - cy) + (bx * bx + by * by) * (cy - ay)
                + (cx * cx + cy * cy) * (ay - by)) / d;
        double uy = ((ax * ax + ay * ay) * (cx - bx) + (bx * bx + by * by) * (ax - cx)
                + (cx * cx + cy * cy) * (bx - ax)) / d;

        return new Point2D(ux, uy);
    }

    private Point2D getBezierPointAtFraction(double fraction) {
        if (controlPoints.size() < 2) {
            return controlPoints.isEmpty() ? new Point2D(0, 0) : controlPoints.get(0);
        }

        // Parse the control points into segments based on anchor points (duplicate of
        // the next point)
        List<List<Point2D>> bezierSegments = new ArrayList<>();
        List<Point2D> currentSegment = new ArrayList<>();
        currentSegment.add(controlPoints.get(0));

        for (int i = 1; i < controlPoints.size(); i++) {
            Point2D currentPoint = controlPoints.get(i);
            currentSegment.add(currentPoint);

            // Check if this is an anchor point (duplicate of the next point)
            boolean isAnchor = i < controlPoints.size() - 1 &&
                    currentPoint.distance(controlPoints.get(i + 1)) < 1e-6;
            boolean isLastPoint = i == controlPoints.size() - 1;

            if (isAnchor || isLastPoint) {
                // Complete this segment
                if (currentSegment.size() >= 2) {
                    bezierSegments.add(new ArrayList<>(currentSegment));
                }

                // Start new segment with the anchor point
                if (isAnchor) {
                    currentSegment.clear();
                    currentSegment.add(currentPoint);
                }
            }
        }

        if (bezierSegments.isEmpty()) {
            // Fallback: treat all points as one segment
            bezierSegments.add(new ArrayList<>(controlPoints));
        }

        // Calculate the total approximate length of all segments
        double[] segmentLengths = new double[bezierSegments.size()];
        double totalLength = 0;

        for (int i = 0; i < bezierSegments.size(); i++) {
            segmentLengths[i] = approximateBezierLength(bezierSegments.get(i));
            totalLength += segmentLengths[i];
        }

        if (totalLength == 0) {
            return controlPoints.get(0);
        }

        // Find which segment the fraction falls into
        double targetDistance = fraction * totalLength;
        double accumulatedDistance = 0;
        int segmentIndex = 0;

        for (int i = 0; i < segmentLengths.length; i++) {
            if (accumulatedDistance + segmentLengths[i] >= targetDistance) {
                segmentIndex = i;
                break;
            }
            accumulatedDistance += segmentLengths[i];
        }

        // Calculate the fraction within the found segment
        double fractionInSegment = 0;
        if (segmentLengths[segmentIndex] > 0) {
            fractionInSegment = (targetDistance - accumulatedDistance) / segmentLengths[segmentIndex];
        }
        fractionInSegment = Math.max(0.0, Math.min(1.0, fractionInSegment));

        // Get the point on the specific segment
        return getPointOnBezierSegment(bezierSegments.get(segmentIndex), fractionInSegment);
    }

    private double approximateBezierLength(List<Point2D> segmentPoints) {
        if (segmentPoints.size() < 2)
            return 0;

        // Approximate length by sampling the curve
        double length = 0;
        int samples = Math.max(10, segmentPoints.size() * 5);
        Point2D prevPoint = getPointOnBezierSegment(segmentPoints, 0);

        for (int i = 1; i <= samples; i++) {
            double t = (double) i / samples;
            Point2D currentPoint = getPointOnBezierSegment(segmentPoints, t);
            length += prevPoint.distance(currentPoint);
            prevPoint = currentPoint;
        }

        return length;
    }

    private Point2D getPointOnBezierSegment(List<Point2D> segmentPoints, double t) {
        if (segmentPoints.size() < 2) {
            return segmentPoints.isEmpty() ? new Point2D(0, 0) : segmentPoints.get(0);
        }

        t = Math.max(0.0, Math.min(1.0, t));

        if (segmentPoints.size() == 2) {
            // Linear interpolation
            return getPointOnLinear(segmentPoints.get(0), segmentPoints.get(1), t);
        } else if (segmentPoints.size() == 3) {
            // Quadratic Bezier
            return getPointOnQuadraticBezier(segmentPoints.get(0), segmentPoints.get(1), segmentPoints.get(2), t);
        } else if (segmentPoints.size() == 4) {
            // Cubic Bezier
            return getPointOnCubicBezier(segmentPoints.get(0), segmentPoints.get(1), segmentPoints.get(2),
                    segmentPoints.get(3), t);
        } else {
            // Higher order Bezier - use De Casteljau's algorithm
            return getPointOnHighOrderBezier(segmentPoints, t);
        }
    }

    private Point2D getPointOnQuadraticBezier(Point2D p0, Point2D p1, Point2D p2, double t) {
        double u = 1.0 - t;
        double tt = t * t;
        double uu = u * u;

        double x = uu * p0.getX() + 2 * u * t * p1.getX() + tt * p2.getX();
        double y = uu * p0.getY() + 2 * u * t * p1.getY() + tt * p2.getY();

        return new Point2D(x, y);
    }

    private Point2D getPointOnHighOrderBezier(List<Point2D> points, double t) {
        // De Casteljau's algorithm for any order Bezier curve
        List<Point2D> temp = new ArrayList<>(points);

        while (temp.size() > 1) {
            List<Point2D> newTemp = new ArrayList<>();
            for (int i = 0; i < temp.size() - 1; i++) {
                Point2D p1 = temp.get(i);
                Point2D p2 = temp.get(i + 1);
                double x = (1 - t) * p1.getX() + t * p2.getX();
                double y = (1 - t) * p1.getY() + t * p2.getY();
                newTemp.add(new Point2D(x, y));
            }
            temp = newTemp;
        }

        return temp.get(0);
    }

    private void updateSliderBallColors() {
        if (sliderBall == null || sliderBallOuter == null) {
            return;
        }

        if (mouseInBallRadius && keyHolded) {
            sliderBall.setFill(Color.PINK.deriveColor(1, 1, 1, 0.7));
            sliderBallOuter.setFill(Color.PINK.deriveColor(1, 1, 1, 0.35));
        } else {
            // darker colors when mouse is not in radius
            sliderBall.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.3)); // dimmer white
            sliderBallOuter.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.15)); // even dimmer
        }
    }
}