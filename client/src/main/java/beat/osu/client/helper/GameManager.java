package beat.osu.client.helper;

import beat.osu.client.enums.GameEventType;
import beat.osu.client.enums.GameState;
import beat.osu.client.enums.HitResult;
import beat.osu.client.factory.HitObjectFactory;
import beat.osu.client.game.ComboChangeData;
import beat.osu.client.game.GameEvent;
import beat.osu.client.game.HitObjectEventData;
import beat.osu.client.game.ScoreChangeData;
import beat.osu.client.interfaces.Observer;
import beat.osu.client.interfaces.Subject;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.HitCircle;
import beat.osu.client.model.HitObject;
import beat.osu.client.utils.OsuParser;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameManager implements Subject {
    private final List<Observer> observerList = new CopyOnWriteArrayList<>();

    private final Beatmap beatmap;
    @Getter
    private final ArrayList<HitObject> hitObjects;
    private AnimationTimer gameLoop;
    private long startTimeNanos = -1;
    private long pauseStartNanos = -1;
    private long totalPausedNanos = 0;
    private long gameStartOffset = 2000;
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
    private int perfectHits = 0;
    private int greatHits = 0;
    private int goodHits = 0;
    private int gekiHits = 0;
    private int greatKatuHits = 0;
    private int misses = 0;
    private double accuracy = 100.0;
    private double health = 100;
    private int highestCombo = 0;
    private boolean perfectCombo = true;
    private boolean imperfectOrMissed = false;

    public void updateMousePosition(double x, double y) {
        this.currentMouseX = x;
        this.currentMouseY = y;
    }

    private void pauseAllAnimations() {
        for (HitObject hitObject : hitObjects) {
            if (hitObject.isVisible() && !hitObject.isHit()) {
                hitObject.pauseAnimations();
            }
        }
    }

    private void resumeAllAnimations() {
        for (HitObject hitObject : hitObjects) {
            if (hitObject.isVisible() && !hitObject.isHit()) {
                hitObject.resumeAnimations();
            }
        }
    }

    public void startGame() {
        if (gameState == GameState.PLAYING) {
            return;
        }

        startTimeNanos = -1;
        totalPausedNanos = 0;
        notifyObservers(new GameEvent(GameEventType.GAME_STARTED, null));
        // sync BGM with game start offset
        PauseTransition bgmSync = new PauseTransition(Duration.millis(gameStartOffset));
        bgmSync.setOnFinished(e -> {
            BgmManager.playGameBgm();
        });
        bgmSync.play();

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

                long elapsedNanos = now - startTimeNanos - totalPausedNanos;
                long elapsedMillis = elapsedNanos / 1_000_000;

                updateGame(elapsedMillis - gameStartOffset);
            }
        };
        gameLoop.start();
    }

    public void pauseGame() {
        if (gameState != GameState.PLAYING) {
            return;
        }

        pauseStartNanos = System.nanoTime();
        gameState = GameState.PAUSED;
        BgmManager.pauseBgm();
        pauseAllAnimations();
        notifyObservers(new GameEvent(GameEventType.GAME_PAUSED, null));
    }

    public void resumeGame() {
        if (gameState != GameState.PAUSED) {
            return;
        }

        // Calculate pause duration
        if (pauseStartNanos != -1) {
            totalPausedNanos += System.nanoTime() - pauseStartNanos;
            pauseStartNanos = -1;
        }

        gameState = GameState.PLAYING;
        BgmManager.resumeBgm();
        // add countdown later
        resumeAllAnimations();
        notifyObservers(new GameEvent(GameEventType.GAME_RESUMED, null));
    }

    public void stopGame() {

    }

    private void updateGame(long elapsedMillis) {
        Set<KeyCode> currentKeys = inputManager.getPressedKeys();

        boolean pressedEsc = currentKeys.contains(KeyCode.ESCAPE) &&
                !previousKeys.contains(KeyCode.ESCAPE);

        if(pressedEsc) {
            if(gameState == GameState.PLAYING) {
                pauseGame();
            } else if(gameState == GameState.PAUSED) {
                resumeGame(); // Use a separate resume method
            }
            previousKeys.clear();
            previousKeys.addAll(currentKeys);
            return;
        }

        // Only process game logic when playing
        if (gameState != GameState.PLAYING) {
            previousKeys.clear();
            previousKeys.addAll(currentKeys);
            return;
        }

        boolean keyPressed = false;
        boolean pressedKeybind1 = currentKeys.contains(InputManager.getKeybind1()) &&
                !previousKeys.contains(InputManager.getKeybind1());
        boolean pressedKeybind2 = currentKeys.contains(InputManager.getKeybind2()) &&
                !previousKeys.contains(InputManager.getKeybind2());
        if (pressedKeybind1 || pressedKeybind2) {
            keyPressed = true;
        }

        Iterator<HitObject> iterator = hitObjects.iterator();
        while(iterator.hasNext()) {
            HitObject hitObject = iterator.next();
            hitObject.update(elapsedMillis);
            if(hitObject.getHitTime() > elapsedMillis + 5000) {
                // If the hit object is still far, skip processing
                break;
            }

            if (hitObject.isVisible() && !hitObject.isHit()) {
                if (keyPressed) {
                    if (checkHitObjectClick(hitObject, elapsedMillis)) {
                        keyPressed = false; // Prevent hitting overlapping objects
                    }
                }

                // Check for miss (object passed its time window)
                if (elapsedMillis > hitObject.getHitTime() + getHitWindow()) {
                    handleMiss(hitObject);
                    iterator.remove(); // Remove hit object after handling miss
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
            long timingError = elapsedMillis - hitObject.getHitTime();
            handleHit(hitObject, timingError);
            return true;
        }
        return false;
    }

    private void updateHighestCombo(int combo) {
        if (combo > highestCombo) {
            highestCombo = combo;
        }
    }

    private void handleHit(HitObject hitObject, long timingError) {
        if(hitObject.isNewCombo()) {
            perfectCombo = true;
            imperfectOrMissed = false;
        }

        hitObject.setHit(true);
        hitObject.playHitEffect();
        // play sfx
        for(String sfx : hitObject.getSfxFilenames()) {
            if(hitObject instanceof HitCircle) SfxManager.playSfx(sfx);
        }

        masterComboNumber++;
        updateHighestCombo(masterComboNumber);

        // Determine hit result based on timing
        HitResult hitResult = HitResult.fromTimingError(timingError);
        if(hitResult == HitResult.PERFECT) {
            if(perfectCombo && hitObject.isComboEnd()) {
                gekiHits++;
            } else {
                perfectHits++;
            }
        }else if(hitResult == HitResult.GREAT) {
            if(!imperfectOrMissed && hitObject.isComboEnd()) {
                greatKatuHits++;
            }
            else greatHits++;
            perfectCombo = false;
        }else if(hitResult == HitResult.GOOD) {
            goodHits++;
            perfectCombo = false;
            imperfectOrMissed = true;
        }

//        System.out.println("perfect hits: " + perfectHits);
//        System.out.println("great hits: " + greatHits);
//        System.out.println("geki hits: " + gekiHits);
//        System.out.println("great katu hits: " + greatKatuHits);
        int hitValue = hitResult.getScore();
//        System.out.println("hit value: " + hitValue);
        int difficultyMultiplier = beatmap.getDifficultyMultiplier(hitObjects, OsuParser.getBreakPointsList());
        int hitScore = hitValue * (1 + (masterComboNumber * difficultyMultiplier));
        score += hitScore;
//        System.out.println(score);

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
                new HitObjectEventData(hitObject, timingError, hitResult,
                        perfectCombo, imperfectOrMissed)));

//        System.out.println("current accuracy: " + accuracy);
        notifyObservers(new GameEvent(GameEventType.ACCURACY_CHANGED, accuracy));
        notifyObservers(new GameEvent(GameEventType.HEALTH_CHANGED, health));
    }

    private void handleMiss(HitObject hitObject) {
        perfectCombo = false;
        imperfectOrMissed = true;
        hitObject.hide();

        misses++;
        int oldCombo = masterComboNumber;
        masterComboNumber = 0;

        // Update accuracy
        updateAccuracy();

        // Update health (missing decreases health)
        health = Math.max(0, health - beatmap.getHpDrainRate());

        // Notify observers
        notifyObservers(new GameEvent(GameEventType.COMBO_CHANGED,
                new ComboChangeData(masterComboNumber, oldCombo > 0)));

        notifyObservers(new GameEvent(GameEventType.HIT_OBJECT_MISSED,
                new HitObjectEventData(hitObject, 0, HitResult.MISS,
                        false, true)));

//        System.out.println("current accuracy: " + accuracy);
        notifyObservers(new GameEvent(GameEventType.ACCURACY_CHANGED, accuracy));
        notifyObservers(new GameEvent(GameEventType.HEALTH_CHANGED, health));

        // Check for game over (health reaches 0)
        if (health <= 0) {
            stopGame();
        }
    }

    private void updateAccuracy() {
        double hitValues = (perfectHits * HitResult.PERFECT.getScore()) +
                (gekiHits * HitResult.PERFECT.getScore()) +
                (greatHits * HitResult.GREAT.getScore()) +
                (greatKatuHits * HitResult.GREAT.getScore()) +
                (goodHits * HitResult.GOOD.getScore());
        double maximumValues = (perfectHits + gekiHits + greatHits + greatKatuHits + goodHits + misses)
                * HitResult.PERFECT.getScore();
        accuracy = maximumValues > 0 ? (hitValues / maximumValues) * 100.0 : 100.0;
    }

    private long getHitWindow() {
        return 300; // 300ms hit window
    }

//    private boolean areAllObjectsProcessed() {
//
//    }

    private void processBeatmap() {
        OsuParser.extractAndParse(beatmap);

        // Reset combo counters
        masterComboNumber = 0;
        currentComboNumberInSet = 0;
        currentComboSetIndex = 0;
        comboSkipCounter = 0;

        ArrayList<String> hitObjectData = OsuParser.getHitObjects();
        for(int i = 0; i < hitObjectData.size(); i++) {
            String data = hitObjectData.get(i);

            boolean comboEnd = false;
            String nextData = (i + 1 < hitObjectData.size()) ? hitObjectData.get(i + 1) : null;
            if(nextData != null) {
                comboEnd = HitObjectFactory.checkNewCombo(nextData);
            }

            createHitObject(data, comboEnd);
        }
    }

    private void createHitObject(String data, boolean comboEnd) {
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
                currentComboNumberInSet, currentComboSetIndex, comboEnd);
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
