package beat.osu.client.helper;

import beat.osu.client.controller.ScoreController;
import beat.osu.client.controller.SessionController;
import beat.osu.client.controller.SpectateController;
import beat.osu.client.enums.GameEventType;
import beat.osu.client.enums.GameState;
import beat.osu.client.enums.HealthRecover;
import beat.osu.client.enums.HitResult;
import beat.osu.client.events.game.*;
import beat.osu.client.factory.HitObjectFactory;
import beat.osu.client.interfaces.game.HitObjectListener;
import beat.osu.client.interfaces.game.GameEventListener;
import beat.osu.client.interfaces.game.GameEventPublisher;
import beat.osu.client.model.*;
import beat.osu.client.utils.OsuParser;
import beat.osu.client.utils.ReplayUtils;
import beat.osu.client.events.game.ReplayEvent;
import beat.osu.shared.dto.game.events.SpectateEvent;
import beat.osu.shared.dto.user.UserDto;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import lombok.Getter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameManager implements GameEventPublisher, HitObjectListener {
    private final List<GameEventListener> gameEventListenerList = new CopyOnWriteArrayList<>();

    private final Beatmap beatmap;
    @Getter
    private final ArrayList<HitObject> hitObjects;
    private AnimationTimer gameLoop;
    private long startTimeNanos = -1;
    private long pauseStartNanos = -1;
    private long totalPausedNanos = 0;
    private final long gameStartOffset = 2000;
    private long lastHpDrainMillis = 0;
    private GameState gameState = GameState.NOT_STARTED;
    private boolean bgmStarted = false;
    private final InputManager inputManager;
    private final ScoreController scoreController;
    private final SessionController sessionController;
    private final SpectateController spectateController;

    private final Set<KeyCode> previousKeys = new HashSet<>();
    private double currentMouseX;
    private double currentMouseY;
    private double paneWidth;
    private double paneHeight;

    // Replay event storage
    @Getter
    private final ArrayList<ReplayEvent> replayEvents = new ArrayList<>();
    private long lastReplayEventTime = -1;

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

    private void createGameSession() {
        System.out.println("Creating game session");
        UserDto user = AuthManager.getUser();
        if (user == null) {
            return;
        }

        sessionController.createPlayingBeatmapSession(user.getId(), beatmap.getBeatmapId()).thenApply(response -> {
            if (response.isSuccess()) {
                System.out.println("Session created successfully: " + response.getValue().getMessage());
            } else {
                System.err.println("Failed to create session: " + response.getError().getMessage());
            }
            return null;
        });
    }

    public void removeGameSession() {
        System.out.println("Removing game session");
        UserDto user = AuthManager.getUser();
        if (user == null) {
            return;
        }
        
        sessionController.removePlayingBeatmapSession(user.getId()).thenApply(response -> {
            if (response.isSuccess()) {
                System.out.println("Session removed successfully: " + response.getValue().getMessage());
            } else {
                System.err.println("Failed to remove session: " + response.getError().getMessage());
            }
            return null;
        });
    }

    public void startGame() {
        if (gameState == GameState.PLAYING) return;

        gameState = GameState.PLAYING;
        bgmStarted = false;
        startTimeNanos = -1;
        totalPausedNanos = 0;

        // Reset replay data
        replayEvents.clear();
        lastReplayEventTime = -1;

        notifyListeners(new GameEvent(GameEventType.GAME_STARTED, null));

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

                if (elapsedMillis > lastHpDrainMillis + 1000 && gameState == GameState.PLAYING) {
                    lastHpDrainMillis = elapsedMillis;
                    health = Math.max(0, health - beatmap.getHpDrainRate());
                    System.out.println("draining health, health: " + health);
                    notifyListeners(new GameEvent(GameEventType.HEALTH_CHANGED, health));
                }

                if (!bgmStarted && elapsedMillis >= gameStartOffset) {
                    System.out.println("Starting BGM playback");
                    BgmManager.getInstance().playGameBgm();
                    bgmStarted = true;
                }

                updateGame(elapsedMillis - gameStartOffset);
            }
        };
        gameLoop.start();
        // create game session
        createGameSession();
    }

    private void pauseGame() {
        System.out.println("pausing game");
//        for (ReplayEvent event : replayEvents) {
//            System.out.println("ReplayEventOsu(time_delta=" + event.getTimeDelta() +
//                    ", x=" + event.getX() + ", y=" + event.getY() +
//                    ", keys=" + event.getKeyMask() + ")");
//        }

        pauseStartNanos = System.nanoTime();
        gameState = GameState.PAUSED;
        BgmManager.getInstance().pauseBgm();
        pauseAllAnimations();
        notifyListeners(new GameEvent(GameEventType.GAME_PAUSED, null));
    }

    public void resumeGame() {
        if (gameState != GameState.PAUSED) return;

        // Calculate pause duration
        if (pauseStartNanos != -1) {
            totalPausedNanos += System.nanoTime() - pauseStartNanos;
            pauseStartNanos = -1;
        }

        gameState = GameState.PLAYING;
        if (bgmStarted) BgmManager.getInstance().resumeBgm();
        // add countdown later
        resumeAllAnimations();
        notifyListeners(new GameEvent(GameEventType.GAME_RESUMED, null));
    }

    public void stopGame() {
        removeGameSession();
        System.out.println("all hit objects processed, stopping game");
        gameState = GameState.COMPLETED;
        gameLoop.stop();
        String grade = calculateGrade();
        System.out.println("Game ended with grade: " + grade);
        LocalDateTime now = LocalDateTime.now();

        notifyListeners(new GameEvent(GameEventType.GAME_ENDED, new GameEndEvent(
                score, highestCombo, accuracy, perfectHits, gekiHits, greatHits, greatKatuHits, goodHits,
                misses, grade, now)));

        UserDto user = AuthManager.getUser();
        if(user == null) return;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formatted = now.format(formatter);
        String osrFileName = String.format("%s-%s-%s.osr",
                user.getId(), beatmap.getBeatmapId(), formatted.replace("/", "-").replace(":", "-"));
        try {
            ReplayUtils.saveReplay(replayEvents, osrFileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        scoreController.insertScore(beatmap.getBeatmapId(), user.getId(), score,
                highestCombo, accuracy, perfectHits, gekiHits, greatHits, greatKatuHits,
                goodHits, misses, grade, now).thenApply(response -> {
            if (response.isSuccess()) {
                System.out.println("Score inserted successfully: " + response.getValue().getMessage());
            } else {
                System.err.println("Failed to insert score: " + response.getError().getMessage());
            }
            return null;
        });
    }

    private void failGame() {
        removeGameSession();
        System.out.println("Game failed, stopping game");
        gameState = GameState.FAILED;
        gameLoop.stop();
        BgmManager.getInstance().stopBgm();
        notifyListeners(new GameEvent(GameEventType.GAME_FAILED, null));
    }

    private void updateGame(long elapsedMillis) {
        Set<KeyCode> currentKeys = inputManager.getPressedKeys();

        // store game information for replay
        System.out.println("Current game time: " + elapsedMillis + " ms");
        // Store replay event data
        storeReplayEvent(elapsedMillis, currentKeys);

        boolean pressedEsc = currentKeys.contains(KeyCode.ESCAPE) &&
                !previousKeys.contains(KeyCode.ESCAPE);

        // validate break period here
        boolean inBreakPeriod = false;
        for (BreakPeriod breakPeriod : OsuParser.getBreakPeriodsList()) {
            int startTime = breakPeriod.getStartTime();
            int endTime = breakPeriod.getEndTime();
            if (elapsedMillis >= startTime && elapsedMillis <= endTime) {
                inBreakPeriod = true;
                if (gameState != GameState.BREAK_PERIOD) {
                    System.out.println("Entering break period");
                    gameState = GameState.BREAK_PERIOD;
                }
                break;
            }
        }

        // Return to playing state if not in break period time
        if (!inBreakPeriod && gameState == GameState.BREAK_PERIOD) {
            System.out.println("Exiting break period, returning to playing state");
            gameState = GameState.PLAYING;
        }

        if (pressedEsc) {
            if (gameState == GameState.PLAYING || gameState == GameState.BREAK_PERIOD) {
                pauseGame();
                // notify to listeners that spectate is paused

            } else if (gameState == GameState.PAUSED) {
                resumeGame();
                // notify to listeners that spectate is resumed

            }
            previousKeys.clear();
            previousKeys.addAll(currentKeys);
        }

        // Only process game logic when playing & break period
        if (gameState != GameState.PLAYING && gameState != GameState.BREAK_PERIOD) {
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
        while (iterator.hasNext()) {
            HitObject hitObject = iterator.next();
            hitObject.update(elapsedMillis);
            if (hitObject instanceof HitSpinner) {
                ((HitSpinner) hitObject).updateSpinner(currentMouseX, currentMouseY);
            } else if (hitObject instanceof HitSlider) {
                ((HitSlider) hitObject).updateSlider(currentMouseX, currentMouseY);
            }

            if (hitObject.getHitTime() > elapsedMillis + 5000) {// skip processing if far
                break;
            }

            if (hitObject.isVisible() && !hitObject.isHit()) {
                if (keyPressed) {
                    if (checkHitObjectClick(hitObject, elapsedMillis)) {
                        keyPressed = false; // Prevent hitting overlapping objects
                    }
                }

                if (hitObject instanceof HitSpinner) break; // Don't handle misses for spinners

                // Check for miss (object passed its time window)
                if (elapsedMillis > hitObject.getHitTime() + getHitWindow()) {
                    handleMiss(hitObject);
                    iterator.remove(); // Remove hit object after handling miss
                    // System.out.println("Removing missed HitObject: " + hitObject);
                    continue;
                }
            }
            if (hitObject.isHit() && !hitObject.isVisible()) {
                // If the hit object is already hit and not visible, remove it
                iterator.remove();
                // System.out.println("Removing HitObject after it was hit and is no longer
                // visible: " + hitObject);
            }
        }

        // Update input overlay
        boolean key1IsPressed = currentKeys.contains(InputManager.getKeybind1());
        boolean key2IsPressed = currentKeys.contains(InputManager.getKeybind2());
        notifyListeners(new GameEvent(GameEventType.INPUT_OVERLAY_CHANGED,
                new InputOverlayEvent(key1IsPressed, key2IsPressed)));

        previousKeys.clear();
        previousKeys.addAll(currentKeys);

        // System.out.println("hit objects remaining: " + hitObjects.size());
        if (hitObjects.isEmpty()) {
            stopGame();
        }
    }

    private void storeReplayEvent(long elapsedMillis, Set<KeyCode> currentKeys) {
        // Calculate time delta
        long timeDelta;
        if (lastReplayEventTime == -1) {
            // First replay event, store current timestamp instead of delta
            timeDelta = elapsedMillis;
        } else {
            timeDelta = elapsedMillis - lastReplayEventTime;
        }

        // Calculate key mask based on keybinds
        int keyMask = 0;
        if (currentKeys.contains(InputManager.getKeybind1())) {
            keyMask |= 1; // Set bit 0 for keybind1
        }
        if (currentKeys.contains(InputManager.getKeybind2())) {
            keyMask |= 2; // Set bit 1 for keybind2
        }

        // Create and store replay event
        ReplayEvent replayEvent = new ReplayEvent(timeDelta, currentMouseX, currentMouseY, keyMask, paneWidth, paneHeight);
        replayEvents.add(replayEvent);

        sendSpectateEvent(elapsedMillis, replayEvent);

        // Update last event time for next delta calculation
        lastReplayEventTime = elapsedMillis;
    }

    private void sendSpectateEvent(long elapsedMillis, ReplayEvent replayEvent) {
        SpectateEvent event = new SpectateEvent(elapsedMillis, replayEvent.getX(),
                replayEvent.getY(), replayEvent.getKeyMask(), paneWidth, paneHeight,
                masterComboNumber, score, accuracy, health);
        spectateController.sendSpectateEvent(event).thenApply(response -> {
            if (response.isSuccess()) {
                System.out.println("Spectate event sent successfully: " + response.getValue().getMessage());
            } else {
                System.err.println("Failed to send spectate event: " + response.getError().getMessage());
            }
            return null;
        });
    }

    private boolean checkHitObjectClick(HitObject hitObject, long elapsedMillis) {
        double objCenterX = hitObject.getScreenCenterX();
        double objCenterY = hitObject.getScreenCenterY();
        double objRadius = hitObject.getScreenRadius();
        if (hitObject instanceof HitSpinner)
            objRadius *= 2.5;

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
        // if (OsuParser.isDoubleTime()) {
        // multiplier *= 1.5; // Double Time
        // }
        // if (OsuParser.isHalfTime()) {
        // multiplier *= 0.75; // Half Time
        // }
        // if (OsuParser.isHardRock()) {
        // multiplier *= 1.06; // Hard Rock
        // }
        // if (OsuParser.isEasy()) {
        // multiplier *= 0.5; // Easy
        // }
        return multiplier;
    }

    private void updateHitCount(HitObject hitObject, HitResult hitResult) {
        if (hitResult != HitResult.SPIN && hitResult != HitResult.COMPLETE_SPIN && hitResult != HitResult.SLIDER_END) {
            // System.out.println("combo naik");
            masterComboNumber++;
            updateHighestCombo(masterComboNumber);
        }

        if (hitResult == HitResult.PERFECT) {
            if (!imperfectOrMissed && hitObject.isComboEnd()) {
                gekiHits++;
            }
            perfectHits++;
        } else if (hitResult == HitResult.GREAT) {
            if (!imperfectOrMissed && hitObject.isComboEnd()) {
                greatKatuHits++;
            }
            greatHits++;
            perfectCombo = false;
        } else if (hitResult == HitResult.GOOD) {
            goodHits++;
            perfectCombo = false;
            imperfectOrMissed = true;
        }
    }

    private void handleHit(HitObject hitObject, long timingError) {
        HitResult hitResult = HitResult.fromTimingError(timingError, beatmap.getOverallDifficulty());

        if (hitObject instanceof HitCircle) hitObject.setVisible(false);
        if (hitObject.isNewCombo()) {
            perfectCombo = true;
            imperfectOrMissed = false;
        }

        if (hitObject instanceof HitSpinner) {
            // System.out.println("hitting spinner, returning");
            hitObject.setHit(true);
            hitObject.playHitEffect();
            return;
        }

        if (hitObject instanceof HitSlider && hitResult == HitResult.MISS) {
            ((HitSlider) hitObject).setEarlyHit(true);
            return;
        }

        hitObject.setHit(true);
        hitObject.playHitEffect();

        if (!(hitObject instanceof HitCircle)) return;
        // play sfx
        for (String sfx : hitObject.getSfxFilenames()) {
            SfxManager.playSfx(sfx);
        }
        // Determine hit result based on timing
        if (hitResult == HitResult.MISS) notifyMiss(hitObject);
        else notifyHit(hitObject, hitResult);
    }

    private HealthRecover getHealthRecover(HitObject hitObject, HitResult hitResult) {
        switch (hitResult) {
            case PERFECT:
                if (hitObject.isComboEnd()) {
                    if (perfectCombo)
                        return HealthRecover.GEKI;
                    else
                        return HealthRecover.PERFECT_KATU;
                } else {
                    return HealthRecover.PERFECT;
                }
            case GREAT:
                if (!imperfectOrMissed && hitObject.isComboEnd()) {
                    return HealthRecover.GREAT_KATU;
                } else {
                    return HealthRecover.GREAT;
                }
            case GOOD:
                return HealthRecover.GOOD;
            case SPIN:
                return HealthRecover.SPIN;
            case COMPLETE_SPIN:
                return HealthRecover.COMPLETE_SPIN;
            default:
                return HealthRecover.NONE;
        }
    }

    private double calculateScoreFactor(HitResult hitResult) {
        if (hitResult != HitResult.PERFECT && hitResult != HitResult.GREAT && hitResult != HitResult.GOOD) {
            return 1.0;
        }

        double comboMultiplier = Math.max(masterComboNumber - 1, 0);
        int difficultyMultiplier = beatmap.getDifficultyMultiplier(hitObjects, OsuParser.getBreakPeriodsList());
        double modMultiplier = getModMultiplier();
        return 1.0 + (comboMultiplier * difficultyMultiplier * modMultiplier / 25.0);
    }

    private void notifyHit(HitObject hitObject, HitResult hitResult) {
        // masterComboNumber++;
        // updateHighestCombo(masterComboNumber);
        updateHitCount(hitObject, hitResult);

        int hitValue = hitResult.getScore();

        double scoreFactor = calculateScoreFactor(hitResult);
        int hitScore = (int) Math.round(hitValue * scoreFactor);
        score += hitScore;
        // System.out.println(score);

        // Update accuracy
        updateAccuracy();

        // Update health based on judgement
        HealthRecover healthRecover = getHealthRecover(hitObject, hitResult);
        double hpRecover = healthRecover.getHpRecover();
        health = Math.min(100, health + hpRecover);

        // Notify observers
        notifyListeners(new GameEvent(GameEventType.SCORE_CHANGED,
                new ScoreChangeEvent(score, hitScore)));
        notifyListeners(new GameEvent(GameEventType.COMBO_CHANGED,
                new ComboChangeEvent(masterComboNumber, false)));
        notifyListeners(new GameEvent(GameEventType.HIT_OBJECT_HIT,
                new HitObjectEvent(hitObject, hitResult,
                        perfectCombo, imperfectOrMissed)));
        notifyListeners(new GameEvent(GameEventType.ACCURACY_CHANGED, accuracy));
        notifyListeners(new GameEvent(GameEventType.HEALTH_CHANGED, health));
    }

    private void handleMiss(HitObject hitObject) {
        if (hitObject instanceof HitSpinner) return;
        notifyMiss(hitObject);
    }

    private void notifyMiss(HitObject hitObject) {
        perfectCombo = false;
        imperfectOrMissed = true;
        hitObject.playMissEffect();

        misses++;
        int oldCombo = masterComboNumber;
        masterComboNumber = 0;

        // Update accuracy
        updateAccuracy();

        // Update health (missing decreases health)
        double hpLoss = (0.12 + 0.04 * beatmap.getHpDrainRate()) * 100;
        // System.out.println("hp loss: " + hpLoss);
        health = Math.max(0, health - hpLoss);

        // Notify observers
        notifyListeners(new GameEvent(GameEventType.COMBO_CHANGED,
                new ComboChangeEvent(masterComboNumber, oldCombo > 0)));
        notifyListeners(new GameEvent(GameEventType.HIT_OBJECT_MISSED,
                new HitObjectEvent(hitObject, HitResult.MISS,
                        false, true)));
        notifyListeners(new GameEvent(GameEventType.ACCURACY_CHANGED, accuracy));
        notifyListeners(new GameEvent(GameEventType.HEALTH_CHANGED, health));

        // Check for game over (health reaches 0)
        if (health <= 0) {
            System.out.println("hp reached 0, stopping game");
//            failGame();
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
        return Math.round(200 - 10 * beatmap.getOverallDifficulty());
    }

    private void processBeatmap() {
        // OsuParser.extractAndParse(beatmap);
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
        for (int i = 0; i < hitObjectData.size(); i++) {
            String data = hitObjectData.get(i);

            boolean comboEnd = false;
            String nextData = (i + 1 < hitObjectData.size()) ? hitObjectData.get(i + 1) : null;
            if (nextData != null) {
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
            // Apply combo skip from the *previous* new combo object, or this one if it's
            // the first.
            // The comboSetIndex is incremented by 1 + the number of colors to skip.
            currentComboSetIndex = (currentComboSetIndex + 1 + comboSkipCounter) % OsuParser.getColours().size();
            comboSkipCounter = comboSkipFromThisObject; // Store skip for NEXT new combo
        } else {
            currentComboNumberInSet++;
        }

        HitObject newHitObject = HitObjectFactory.createHitObject(data, beatmap,
                currentComboNumberInSet, currentComboSetIndex, comboEnd, this);
        hitObjects.add(newHitObject);
    }

    public GameManager(Beatmap beatmap, InputManager inputManager, double paneWidth, double paneHeight) {
        this.beatmap = beatmap;
        this.inputManager = inputManager;
        this.paneWidth = paneWidth;
        this.paneHeight = paneHeight;
        this.hitObjects = new ArrayList<>();
        this.scoreController = new ScoreController();
        this.sessionController = new SessionController();
        this.spectateController = new SpectateController();

        processBeatmap();
    }

    @Override
    public void addListener(GameEventListener gameEventListener) {
        gameEventListenerList.add(gameEventListener);
    }

    @Override
    public void removeListener(GameEventListener gameEventListener) {
        gameEventListenerList.remove(gameEventListener);
    }

    @Override
    public void notifyListeners(GameEvent event) {
        for (GameEventListener gameEventListener : gameEventListenerList) {
            gameEventListener.update(event);
        }
    }

    @Override
    public void onHit(HitObject hitObject, HitResult result) {
        System.out.println("on hit");
        notifyHit(hitObject, result);
    }

    @Override
    public void onMiss(HitObject hitObject) {
        notifyMiss(hitObject);
    }

    @Override
    public void onAdditionalSpin(HitObject hitObject, int additionalSpin) {
        notifyListeners(new GameEvent(GameEventType.ADDITIONAL_SPIN,
                new AdditionalSpinEvent(hitObject, additionalSpin)));
    }

    @Override
    public void onSliderTick(HitObject hitObject) {
        System.out.println("on slider tick");
        notifyHit(hitObject, HitResult.SLIDER_TICK);
    }

    @Override
    public void onSliderRepeat(HitObject hitObject) {
        System.out.println("on slider repeat");
        notifyHit(hitObject, HitResult.SLIDER_REPEAT);
    }

    @Override
    public void onSliderEnd(HitObject hitObject) {
        System.out.println("on slider end");
        notifyHit(hitObject, HitResult.SLIDER_END);
    }
}
