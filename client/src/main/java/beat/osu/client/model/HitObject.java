package beat.osu.client.model;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import lombok.Data;

import java.util.ArrayList;

@Data
public abstract class HitObject {
    private int osuX;
    private int osuY;
    private long hitTime;
    private int type;
    private int hitSound;
    private String hitSample;
    private int comboNumber;
    private int comboSetIndex;
    private ArrayList<String> sfxFilenames;

    private boolean hit = false;
    private boolean visible = false;
    private long currTime;

    private long spawnTime; // Store the spawn time
    private int preempt;   // ms before hitTime when object appears
    private int fadeIn;    // ms for the object to fade in
    private double circleRadius; // Circle size (CS) for the object

    private double screenCenterX;
    private double screenCenterY;
    private double screenRadius;

    public HitObject(int osuX, int osuY, long hitTime, int type, int hitSound,
                     String hitSample, double approachRate, double circleSize,
                     int comboNumber, int comboSetIndex, ArrayList<String> sfxFilenames) {
        this.osuX = osuX;
        this.osuY = osuY;
        this.hitTime = hitTime;
        this.type = type;
        this.hitSound = hitSound;
        this.hitSample = hitSample;
        this.preempt = calculatePreempt(approachRate);
        this.fadeIn = calculateFadeIn(approachRate);
        this.spawnTime = hitTime - preempt;
        this.circleRadius = calculateCircleRadius(circleSize);
        this.comboNumber = comboNumber;
        this.comboSetIndex = comboSetIndex;
        this.sfxFilenames = sfxFilenames != null ? sfxFilenames : new ArrayList<>();
    }

    public void updateVisuals(double centerX, double centerY, double scaledRadius) {
        this.screenCenterX = centerX;
        this.screenCenterY = centerY;
        this.screenRadius = scaledRadius;
        applyVisualsToNode(centerX, centerY, scaledRadius); // Call the abstract method
    }

    private double calculateCircleRadius(double circleSize) {
        return 54.4 - (4.48 * circleSize);
    }

    private int calculatePreempt(double AR) {
        if (AR < 5.0) {
            return (int)(1200 + 600 * (5 - AR) / 5.0);
        } else if (AR == 5.0) {
            return 1200;
        } else {
            return (int)(1200 - 750 * (AR - 5) / 5.0);
        }
    }

    private int calculateFadeIn(double AR) {
        if (AR < 5.0) {
            return (int)(800 + 400 * (5 - AR) / 5.0);
        } else if (AR == 5.0) {
            return 800;
        } else {
            return (int)(800 - 500 * (AR - 5) / 5.0);
        }
    }

    protected Color parseColorString(String colorString) {
        String[] rgb = colorString.split(",");
        return Color.rgb(
                Integer.parseInt(rgb[0].trim()),
                Integer.parseInt(rgb[1].trim()),
                Integer.parseInt(rgb[2].trim())
        );
    }

    public abstract Node getNode();
    public abstract void update(long currentTime);
    public abstract void handleEvent();
    public abstract void playHitEffect();
    public abstract void applyVisualsToNode(double centerX, double centerY, double scaledRadius);
    public abstract void appear();
    public abstract void hide();
}
