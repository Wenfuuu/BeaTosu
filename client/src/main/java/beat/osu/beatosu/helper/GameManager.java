package beat.osu.beatosu.helper;

import beat.osu.beatosu.enums.GameState;
import beat.osu.beatosu.factory.HitObjectFactory;
import beat.osu.beatosu.game.GameEvent;
import beat.osu.beatosu.interfaces.Observer;
import beat.osu.beatosu.interfaces.Subject;
import beat.osu.beatosu.model.Beatmap;
import beat.osu.beatosu.model.HitObject;
import beat.osu.beatosu.utils.OsuParser;
import beat.osu.beatosu.utils.OszExtractor;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class GameManager implements Subject {
    private List<Observer> observerList;

    private final Beatmap beatmap;
    private final List<HitObject> hitObjects;
    private AnimationTimer gameLoop;
    private long startTimeNanos = -1;
    private GameState gameState = GameState.NOT_STARTED;

    private final Set<KeyCode> previousKeys = new HashSet<>();
    private double currentMouseX;
    private double currentMouseY;

    private int masterComboNumber = 0;
    private int currentComboNumberInSet = 0;
    private int currentComboSetIndex = 0;
    private int comboSkipCounter = 0;

    private int score = 0;
    private int hits = 0;
    private int misses = 0;
    private double accuracy = 100.0;
    private int health = 100;

    public void updateMousePosition(double x, double y) {
        this.currentMouseX = x;
        this.currentMouseY = y;
    }

    public void startGame() {

    }

    public void pauseGame() {

    }

    public void stopGame() {

    }

    private void updateGame() {

    }

    private void checkHitObjectClick(HitObject hitObject, long elapsedMillis) {
        double objCenterX = hitObject.getScreenCenterX();
        double objCenterY = hitObject.getScreenCenterY();
        double objRadius = hitObject.getScreenRadius();

        double dx = currentMouseX - objCenterX;
        double dy = currentMouseY - objCenterY;
        double distanceSquared = (dx * dx) + (dy * dy);

        if (distanceSquared <= objRadius * objRadius) {
            // Valid hit
            hitObject.setHit(true);
            hitObject.playHitEffect();

            long timingError = elapsedMillis - hitObject.getHitTime();
            handleHit(hitObject, timingError);
        }
    }

    private void handleHit(HitObject hitObject, long timingError) {

    }

    private void handleMiss(HitObject hitObject) {

    }

    private void updateAccuracy() {
        int totalObjects = hits + misses;
        if (totalObjects > 0) {
            accuracy = (double) hits / totalObjects * 100.0;
        }
    }

    private long getHitWindow() {
        return 200; // 200ms hit window
    }

//    private boolean areAllObjectsProcessed() {
//
//    }

    private void processBeatmap() {
        // Extract .osz file
        String oszPath = String.format("./src/main/resources/assets/beatmap/%d %s - %s.osz",
                beatmap.getBeatmapSet().getBeatmapSetId(),
                beatmap.getBeatmapSet().getArtist(),
                beatmap.getBeatmapSet().getTitle());
        File oszFile = new File(oszPath);
        File outputDir = new File("./src/main/resources/assets/temp");

        try {
            OszExtractor.extractOsz(oszFile, outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Parse .osu file
        String osuPath = String.format("./src/main/resources/assets/temp/%s - %s (%s) [%s].osu",
                beatmap.getBeatmapSet().getArtist(),
                beatmap.getBeatmapSet().getTitle(),
                beatmap.getBeatmapSet().getCreator(),
                beatmap.getVersion());
        File osuFile = new File(osuPath);

        try {
            OsuParser.parse(osuFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Reset combo counters
        masterComboNumber = 0;
        currentComboNumberInSet = 0;
        currentComboSetIndex = 0;
        comboSkipCounter = 0;

        for (String data : OsuParser.getHitObjects()) {
            createHitObject(data);
        }
    }

    private void createHitObject(String data) {
        boolean isThisObjectANewCombo = HitObjectFactory.checkNewCombo(data);
        int comboSkipFromThisObject = HitObjectFactory.getComboSkipCount(data);

        if (isThisObjectANewCombo) {
            currentComboNumberInSet = 1; // Reset number for this new combo set
            // Apply combo skip from the *previous* new combo object, or this one if it's the first.
            // The comboSetIndex is incremented by 1 + the number of colors to skip.
            currentComboSetIndex = (currentComboSetIndex + 1 + comboSkipCounter) % OsuParser.getColours().size(); // Modulo beatmap's combo color count
            comboSkipCounter = comboSkipFromThisObject; // Store skip for NEXT new combo
        } else {
            currentComboNumberInSet++;
        }

        HitObject newHitObject = HitObjectFactory.createHitObject(data, beatmap,
                currentComboNumberInSet, currentComboSetIndex);
        hitObjects.add(newHitObject);
    }

    public GameManager(Beatmap beatmap) {
        this.beatmap = beatmap;
        this.hitObjects = new ArrayList<>();
        processBeatmap();
    }

    @Override
    public void addObserver(Observer observer) {
        observerList.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observerList.remove(observer);
    }

    @Override
    public void notifyObservers(GameEvent event) {
        for(Observer observer : observerList) {
            observer.update(event);
        }
    }
}
