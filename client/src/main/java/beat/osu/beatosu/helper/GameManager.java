package beat.osu.beatosu.helper;

import beat.osu.beatosu.enums.GameEventType;
import beat.osu.beatosu.enums.GameState;
import beat.osu.beatosu.enums.HitResult;
import beat.osu.beatosu.factory.HitObjectFactory;
import beat.osu.beatosu.game.ComboChangeData;
import beat.osu.beatosu.game.GameEvent;
import beat.osu.beatosu.game.HitObjectEventData;
import beat.osu.beatosu.game.ScoreChangeData;
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
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class GameManager implements Subject {
    private List<Observer> observerList = new CopyOnWriteArrayList<>();

    private final Beatmap beatmap;
    private final List<HitObject> hitObjects;
    private AnimationTimer gameLoop;
    private long startTimeNanos = -1;
    private GameState gameState = GameState.NOT_STARTED;
    private final InputManager inputManager;

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
    private double health = 100;

    public void updateMousePosition(double x, double y) {
        this.currentMouseX = x;
        this.currentMouseY = y;
    }

    public void startGame() {
        if (gameState == GameState.PLAYING) {
            return;
        }

        if (gameState == GameState.NOT_STARTED) {
            startTimeNanos = -1;
            notifyObservers(new GameEvent(GameEventType.GAME_STARTED, null));
        } else if (gameState == GameState.PAUSED) {
            notifyObservers(new GameEvent(GameEventType.GAME_RESUMED, null));
        }

        gameState = GameState.PLAYING;

        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (startTimeNanos == -1) {
                    startTimeNanos = now;
                }

                long elapsedNanos = now - startTimeNanos;
                long elapsedMillis = elapsedNanos / 1_000_000;

                updateGame(elapsedMillis);
            }
        };

        gameLoop.start();
    }

    public void pauseGame() {
        if (gameState != GameState.PLAYING) {
            return;
        }

        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameState = GameState.PAUSED;
        notifyObservers(new GameEvent(GameEventType.GAME_PAUSED, null));
    }

    public void stopGame() {

    }

    private void updateGame(long elapsedMillis) {
        Set<KeyCode> currentKeys = inputManager.getPressedKeys();

        boolean keyPressed = false;
        boolean pressedKeybind1 = currentKeys.contains(InputManager.getKeybind1()) &&
                !previousKeys.contains(InputManager.getKeybind1());
        boolean pressedKeybind2 = currentKeys.contains(InputManager.getKeybind2()) &&
                !previousKeys.contains(InputManager.getKeybind2());
        if (pressedKeybind1 || pressedKeybind2) {
            keyPressed = true;
        }

        for (HitObject hitObject : new ArrayList<>(hitObjects)) {
            hitObject.update(elapsedMillis);

            if (hitObject.isVisible() && !hitObject.isHit()) {
                if (keyPressed) {
                    if (checkHitObjectClick(hitObject, elapsedMillis)) {
                        keyPressed = false; // Prevent hitting multiple objects with one keypress
                    }
                }

                // Check for miss (object passed its time window)
                if (elapsedMillis > hitObject.getHitTime() + getHitWindow()) {
                    handleMiss(hitObject);
                }
            }
        }

        previousKeys.clear();
        previousKeys.addAll(currentKeys);
    }

    private boolean checkHitObjectClick(HitObject hitObject, long elapsedMillis) {
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
            return true;
        }
        return false;
    }

    private void handleHit(HitObject hitObject, long timingError) {
        hits++;
        masterComboNumber++;

        // Determine hit result based on timing
        HitResult hitResult = HitResult.fromTimingError(timingError);
        int hitScore = hitResult.getScore();
        score += hitScore;

        // Update accuracy
        updateAccuracy();

        // Update health (hitting increases health)
        health = Math.min(100, health + 2);

        // Notify observers
        notifyObservers(new GameEvent(GameEventType.SCORE_CHANGED,
                new ScoreChangeData(score, hitScore)));

        notifyObservers(new GameEvent(GameEventType.COMBO_CHANGED,
                new ComboChangeData(masterComboNumber, false)));

        notifyObservers(new GameEvent(GameEventType.HIT_OBJECT_HIT,
                new HitObjectEventData(hitObject, timingError, hitResult)));

        notifyObservers(new GameEvent(GameEventType.ACCURACY_CHANGED, accuracy));
        notifyObservers(new GameEvent(GameEventType.HEALTH_CHANGED, health));
    }

    private void handleMiss(HitObject hitObject) {
        misses++;
        int oldCombo = masterComboNumber;
        masterComboNumber = 0; // Reset combo on miss

        // Update accuracy
        updateAccuracy();

        // Update health (missing decreases health)
        health = Math.max(0, health - beatmap.getHpDrainRate());

        // Notify observers
        notifyObservers(new GameEvent(GameEventType.COMBO_CHANGED,
                new ComboChangeData(masterComboNumber, oldCombo > 0)));

        notifyObservers(new GameEvent(GameEventType.HIT_OBJECT_MISSED,
                new HitObjectEventData(hitObject, 0, HitResult.MISS)));

        notifyObservers(new GameEvent(GameEventType.ACCURACY_CHANGED, accuracy));
        notifyObservers(new GameEvent(GameEventType.HEALTH_CHANGED, health));

        // Check for game over (health reaches 0)
        if (health <= 0) {
            stopGame();
        }
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

    public GameManager(Beatmap beatmap, InputManager inputManager) {
        this.beatmap = beatmap;
        this.inputManager = inputManager;
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
