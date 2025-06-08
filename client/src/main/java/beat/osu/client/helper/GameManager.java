package beat.osu.client.helper;

import beat.osu.client.enums.GameEventType;
import beat.osu.client.enums.GameState;
import beat.osu.client.enums.HitResult;
import beat.osu.client.factory.HitObjectFactory;
import beat.osu.client.game.*;
import beat.osu.client.interfaces.Observer;
import beat.osu.client.interfaces.Subject;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.HitCircle;
import beat.osu.client.model.HitObject;
import beat.osu.client.model.HitSpinner;
import beat.osu.client.utils.OsuParser;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import lombok.Getter;

import java.io.IOException;
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
    private final long gameStartOffset = 2000;
    private GameState gameState = GameState.NOT_STARTED;
    private boolean bgmStarted = false;
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

    private String calculateGrade() {
        int hitObjectsCount = OsuParser.getHitObjects().size();
        boolean noMiss = (misses == 0);
        double perfectPercentage = (double) (perfectHits + gekiHits) / hitObjectsCount * 100;
        double goodPercentage = (double) goodHits / hitObjectsCount * 100;
        if (accuracy == 100) {
            return "SS";
        } else if (noMiss && perfectPercentage > 90 && goodPercentage <= 1) {
            return "S";
        } else if ((noMiss && perfectPercentage > 80) || perfectPercentage > 90) {
            return "A";
        } else if ((noMiss && perfectPercentage > 70) || perfectPercentage > 80) {
            return "B";
        } else if (perfectPercentage > 60) {
            return "C";
        } else {
            return "D";
        }
    }

    public void startGame() {
        if (gameState == GameState.PLAYING) {
            return;
        }

        gameState = GameState.PLAYING;
        bgmStarted = false;
        startTimeNanos = -1;
        totalPausedNanos = 0;
        notifyObservers(new GameEvent(GameEventType.GAME_STARTED, null));

        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameState == GameState.PAUSED) {
                    return;
                }
                if (startTimeNanos == -1) {
                    startTimeNanos = now;
                }

                long elapsedNanos = now - startTimeNanos - totalPausedNanos;
                long elapsedMillis = elapsedNanos / 1_000_000;

                if (!bgmStarted && elapsedMillis >= gameStartOffset) {
                    BgmManager.playGameBgm();
                    bgmStarted = true;
                }

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
        if(bgmStarted) BgmManager.resumeBgm();
        // add countdown later
        resumeAllAnimations();
        notifyObservers(new GameEvent(GameEventType.GAME_RESUMED, null));
    }

    public void stopGame() {
        System.out.println("all hit objects processed, stopping game");
        gameState = GameState.COMPLETED;
        gameLoop.stop();
        String grade = calculateGrade();
        System.out.println("Game ended with grade: " + grade);

        notifyObservers(new GameEvent(GameEventType.GAME_ENDED, new GameEndData(
                score, perfectHits, gekiHits, greatHits, greatKatuHits, goodHits,
                misses, highestCombo, accuracy, grade
        )));
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
            if(hitObject instanceof HitSpinner) {
                ((HitSpinner) hitObject).updateSpinner(currentMouseX, currentMouseY, this);
            }
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

                if(hitObject instanceof HitSpinner) break; // Don't handle misses for spinners
                // Check for miss (object passed its time window)
                if (elapsedMillis > hitObject.getHitTime() + getHitWindow()) {
                    handleMiss(hitObject);
                    iterator.remove(); // Remove hit object after handling miss
                    System.out.println("Removing missed HitObject: " + hitObject);
                }
            }
            if(hitObject.isHit() && !hitObject.isVisible()) {
                // If the hit object is already hit and not visible, remove it
                iterator.remove();
                System.out.println("Removing HitObject after it was hit and is no longer visible: " + hitObject);
            }
        }

        previousKeys.clear();
        previousKeys.addAll(currentKeys);

//        System.out.println("hit objects remaining: " + hitObjects.size());
        if(hitObjects.isEmpty()) {
            stopGame();
        }
    }

    private boolean checkHitObjectClick(HitObject hitObject, long elapsedMillis) {
        double objCenterX = hitObject.getScreenCenterX();
        double objCenterY = hitObject.getScreenCenterY();
        double objRadius = hitObject.getScreenRadius();
        if(hitObject instanceof HitSpinner) objRadius *= 2.5;

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

    private double getModMultiplier() {
        double multiplier = 1.0;
//        if (OsuParser.isDoubleTime()) {
//            multiplier *= 1.5; // Double Time
//        }
//        if (OsuParser.isHalfTime()) {
//            multiplier *= 0.75; // Half Time
//        }
//        if (OsuParser.isHardRock()) {
//            multiplier *= 1.06; // Hard Rock
//        }
//        if (OsuParser.isEasy()) {
//            multiplier *= 0.5; // Easy
//        }
        return multiplier;
    }

    private void updateHitCount(HitObject hitObject, HitResult hitResult) {
        if(hitResult != HitResult.SPIN && hitResult != HitResult.COMPLETE_SPIN) {
            masterComboNumber++;
            updateHighestCombo(masterComboNumber);
        }

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
    }

    private void handleHit(HitObject hitObject, long timingError) {
        hitObject.setHit(true);
        hitObject.playHitEffect();

        if(hitObject instanceof HitSpinner) {
            System.out.println("hitting spinner, returning");
            return;
        }
        if(hitObject instanceof HitCircle) hitObject.setVisible(false);
        if(hitObject.isNewCombo()) {
            perfectCombo = true;
            imperfectOrMissed = false;
        }

        // play sfx
        if(hitObject instanceof HitCircle) {
            for(String sfx : hitObject.getSfxFilenames()) {
                SfxManager.playSfx(sfx);
            }
        }

        // Determine hit result based on timing
        HitResult hitResult = HitResult.fromTimingError(timingError, beatmap.getOverallDifficulty());
        notifyHit(hitObject, hitResult);
    }

    public void notifyHit(HitObject hitObject, HitResult hitResult) {
//        masterComboNumber++;
//        updateHighestCombo(masterComboNumber);
        updateHitCount(hitObject, hitResult);

        int hitValue = hitResult.getScore();
        double comboMultiplier = Math.max(masterComboNumber - 1, 0);
        int difficultyMultiplier = beatmap.getDifficultyMultiplier(hitObjects, OsuParser.getBreakPointsList());
        double modMultiplier = getModMultiplier();
        double scoreFactor = 1 + (comboMultiplier * difficultyMultiplier * modMultiplier / 25.0);
        int hitScore = (int) Math.round(hitValue * scoreFactor);
        score += hitScore;
//        System.out.println(score);

        // Update accuracy
        updateAccuracy();

        // Update health (hitting increases health)
//        health = Math.min(100, health + 2);

        // Notify observers
        notifyObservers(new GameEvent(GameEventType.SCORE_CHANGED,
                new ScoreChangeData(score, hitScore)));
        notifyObservers(new GameEvent(GameEventType.COMBO_CHANGED,
                new ComboChangeData(masterComboNumber, false)));
        notifyObservers(new GameEvent(GameEventType.HIT_OBJECT_HIT,
                new HitObjectEventData(hitObject, hitResult,
                        perfectCombo, imperfectOrMissed)));
        notifyObservers(new GameEvent(GameEventType.ACCURACY_CHANGED, accuracy));
        notifyObservers(new GameEvent(GameEventType.HEALTH_CHANGED, health));
    }

    public void notifyAdditionalSpin(int totalRotation) {
        System.out.println("total additional rotations: " + totalRotation);
    }

    private void handleMiss(HitObject hitObject) {
        if(hitObject instanceof HitSpinner) return;
        notifyMiss(hitObject);
    }

    public void notifyMiss(HitObject hitObject) {
        perfectCombo = false;
        imperfectOrMissed = true;
        hitObject.playMissEffect();

        misses++;
        int oldCombo = masterComboNumber;
        masterComboNumber = 0;

        // Update accuracy
        updateAccuracy();

        // Update health (missing decreases health)
//        health = Math.max(0, health - beatmap.getHpDrainRate());

        // Notify observers
        notifyObservers(new GameEvent(GameEventType.COMBO_CHANGED,
                new ComboChangeData(masterComboNumber, oldCombo > 0)));
        notifyObservers(new GameEvent(GameEventType.HIT_OBJECT_MISSED,
                new HitObjectEventData(hitObject, HitResult.MISS,
                        false, true)));
        notifyObservers(new GameEvent(GameEventType.ACCURACY_CHANGED, accuracy));
        notifyObservers(new GameEvent(GameEventType.HEALTH_CHANGED, health));

        // Check for game over (health reaches 0)
        if (health <= 0) {
            System.out.println("hp reached 0, stopping game");
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

    private void processBeatmap() {
//        OsuParser.extractAndParse(beatmap);
        try {
            OsuParser.parseBeatmap(beatmap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
