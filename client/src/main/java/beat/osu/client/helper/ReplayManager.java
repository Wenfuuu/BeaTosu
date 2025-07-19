package beat.osu.client.helper;

import beat.osu.client.enums.*;
import beat.osu.client.events.game.*;
import beat.osu.client.factory.HitObjectFactory;
import beat.osu.client.interfaces.game.HitObjectListener;
import beat.osu.client.interfaces.game.GameEventListener;
import beat.osu.client.interfaces.game.GameEventPublisher;
import beat.osu.client.interfaces.game.CoordinateConverter;
import beat.osu.client.model.*;
import beat.osu.client.utils.OsuParser;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReplayManager implements GameEventPublisher, HitObjectListener {
    private final List<GameEventListener> gameEventListenerList = new CopyOnWriteArrayList<>();

    private final Beatmap beatmap;
    @Getter
    private final ArrayList<HitObject> hitObjects;
    private HitObject firstHitObject;
    private AnimationTimer replayLoop;
    private long startTimeNanos = -1;
    private long pauseStartNanos = -1;
    private long totalPausedNanos = 0;
    private final long replayStartOffset = 2000;
    private boolean replayOffsetCompleted = false;
    private long lastHpDrainMillis;
    private GameState gameState = GameState.PLAYING;
    private ReplayState replayState = ReplayState.NOT_STARTED;
    private boolean bgmStarted = false;
    private final ArrayList<ReplayEvent> replayEvents;
    private final InputManager inputManager;
    private final CoordinateConverter coordinateConverter;

    private double currentMouseX;
    private double currentMouseY;

    // Replay event processing fields
    private int currentReplayEventIndex = 0;
    private long accumulatedReplayTime = -2000;
    private boolean wasKey1Pressed = false;
    private boolean wasKey2Pressed = false;
    private boolean keyHolded = false;

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
    private boolean isPreExit = false;
    private boolean isHalfBreakperiod = false;

    private void updateMousePosition(double x, double y) {
        this.currentMouseX = coordinateConverter.convertReplayMouseX(x);
        this.currentMouseY = coordinateConverter.convertReplayMouseY(y);
        System.out.println("ReplayManager: Original coordinates X=" + x + ", Y=" + y);
        System.out.println("ReplayManager: Converted coordinates X=" + currentMouseX + ", Y=" + currentMouseY);
        notifyListeners(new GameEvent(GameEventType.CURSOR_MOVED, new CursorMoveEvent(currentMouseX, currentMouseY)));
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

    public void startReplay() {
        if (replayState == ReplayState.PLAYING)
            return;

        replayState = ReplayState.PLAYING;
        bgmStarted = false;
        startTimeNanos = -1;
        totalPausedNanos = 0;
        replayOffsetCompleted = false;

        // Reset replay event processing
        currentReplayEventIndex = 0;
        accumulatedReplayTime = -2000;
        wasKey1Pressed = false;
        wasKey2Pressed = false;

        notifyListeners(new GameEvent(GameEventType.REPLAY_STARTED, null));

        replayLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (replayState == ReplayState.PAUSED) {
                    return;
                }
                if (startTimeNanos == -1) {
                    startTimeNanos = now;
                }

                long elapsedNanos = now - startTimeNanos - totalPausedNanos;
                long elapsedMillis = elapsedNanos / 1_000_000;

                if (elapsedMillis > lastHpDrainMillis + 1000 && replayState == ReplayState.PLAYING
                && elapsedMillis - replayStartOffset > firstHitObject.getHitTime()) {
                    lastHpDrainMillis = elapsedMillis;
                    health = Math.max(0, health - beatmap.getHpDrainRate());
                    System.out.println("draining health, health: " + health);
                    notifyListeners(new GameEvent(GameEventType.HEALTH_CHANGED, health));
                }

                if (!bgmStarted && elapsedMillis >= replayStartOffset) {
                    BgmManager.getInstance().playGameBgm();
                    bgmStarted = true;
                }

                System.out.println("Elapsed replay millis: " + elapsedMillis);
                if (!replayOffsetCompleted && elapsedMillis >= replayStartOffset) {
                    System.out.println("Replay offset completed, notifying listeners");
                    notifyListeners(new GameEvent(GameEventType.GAME_OFFSET_COMPLETED, null));
                    replayOffsetCompleted = true;
                }

                updateReplay(elapsedMillis - replayStartOffset);
            }
        };
        replayLoop.start();
    }

    private void pauseReplay() {
        pauseStartNanos = System.nanoTime();
        replayState = ReplayState.PAUSED;
        BgmManager.getInstance().pauseBgm();
        pauseAllAnimations();
        notifyListeners(new GameEvent(GameEventType.REPLAY_PAUSED, null));
    }

    public void resumeReplay() {
        if (replayState != ReplayState.PAUSED)
            return;

        if (pauseStartNanos != -1) {
            totalPausedNanos += System.nanoTime() - pauseStartNanos;
            pauseStartNanos = -1;
        }

        replayState = ReplayState.PLAYING;
        if (bgmStarted)
            BgmManager.getInstance().resumeBgm();
        resumeAllAnimations();
        notifyListeners(new GameEvent(GameEventType.REPLAY_RESUMED, null));
    }

    public void stopReplay() {
        replayState = ReplayState.COMPLETED;
        replayLoop.stop();
        notifyListeners(new GameEvent(GameEventType.REPLAY_ENDED, null));
    }

    private void updateReplay(long elapsedMillis) {
        System.out.println("Updating replay, elapsed millis: " + elapsedMillis);
        Set<KeyCode> currentKeys = inputManager.getPressedKeys();
        boolean pressedEsc = currentKeys.contains(KeyCode.ESCAPE);

        boolean inBreakPeriod = false;
        for (BreakPeriod breakPeriod : OsuParser.getBreakPeriodsList()) {
            int startTime = breakPeriod.getStartTime();
            int endTime = breakPeriod.getEndTime();
            if (elapsedMillis >= startTime && elapsedMillis <= endTime) {
                inBreakPeriod = true;
                if (gameState != GameState.BREAK_PERIOD) {
                    System.out.println("Entering break period");
                    gameState = GameState.BREAK_PERIOD;
                    notifyListeners(new GameEvent(GameEventType.ENTER_BREAK_PERIOD, null));
                } else {
                    int totalBreakTime = endTime - startTime;
                    // check if elapsedMillis has passed half of the break period
                    if (totalBreakTime >= 3000 && elapsedMillis >= startTime + totalBreakTime / 2) {
                        if (!isHalfBreakperiod) {
                            System.out.println("Half break period reached, notifying listeners");

                            if (health < 50) {
                                SfxManager.playBeatmapSfx("sectionfail.wav");
                                notifyListeners(new GameEvent(GameEventType.SECTION_FAIL, null));
                            } else {
                                SfxManager.playBeatmapSfx("sectionpass.wav");
                                notifyListeners(new GameEvent(GameEventType.SECTION_PASS, null));
                            }
                            isHalfBreakperiod = true;
                        }
                    }

                    if (elapsedMillis + 1000 >= endTime) {
                        System.out.println("Exiting break period soon, preparing to resume");
                        if (!isPreExit) {
                            notifyListeners(new GameEvent(GameEventType.PRE_EXIT_BREAK_PERIOD, null));
                            isPreExit = true;
                        }
                    }
                }
                break;
            }
        }

        if (!inBreakPeriod && gameState == GameState.BREAK_PERIOD) {
            System.out.println("Exiting break period, returning to playing state");
            isHalfBreakperiod = false;
            isPreExit = false;
            gameState = GameState.PLAYING;
            notifyListeners(new GameEvent(GameEventType.EXIT_BREAK_PERIOD, null));
        }

        if (pressedEsc) {
            if (replayState == ReplayState.PLAYING) {
                pauseReplay();
            } else if (replayState == ReplayState.PAUSED) {
                resumeReplay();
            }
        }

        if (replayState != ReplayState.PLAYING) {
            System.out.println("Replay state is not PLAYING, returning");
            return;
        }

        boolean keyPressed = processReplayEvents(elapsedMillis);
        // System.out.println("key pressed: " + keyPressed);

        Iterator<HitObject> iterator = hitObjects.iterator();
        while (iterator.hasNext()) {
            HitObject hitObject = iterator.next();
            hitObject.update(elapsedMillis);
            if (hitObject instanceof HitSpinner) {
                if (elapsedMillis >= hitObject.getHitTime())
                    ((HitSpinner) hitObject).updateSpinner(currentMouseX, currentMouseY, keyHolded);
            } else if (hitObject instanceof HitSlider) {
                ((HitSlider) hitObject).updateSlider(currentMouseX, currentMouseY, keyHolded);
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

                if (hitObject instanceof HitSpinner)
                    break;
                if (elapsedMillis > hitObject.getHitTime() + getHitWindow()) {
                    handleMiss(hitObject);
                    iterator.remove();
                    continue;
                }
            }
            if (hitObject.isHit() && !hitObject.isVisible()) {
                iterator.remove();
            }
        }

        if (hitObjects.isEmpty()) {
            stopReplay();
        }
    }

    private boolean processReplayEvents(long elapsedMillis) {
        boolean keyPressed = false;

        // Process all replay events that should have occurred by now
        while (currentReplayEventIndex < replayEvents.size()) {
            System.out.println("current replay event index: " + currentReplayEventIndex);
            System.out.println("accumulated replay time: " + accumulatedReplayTime);
            ReplayEvent event = replayEvents.get(currentReplayEventIndex);

            if (accumulatedReplayTime > elapsedMillis) {
                break;
            }

            if (currentReplayEventIndex == 0) {
                // First event uses absolute time
                accumulatedReplayTime = event.getTimeDelta();
            } else {
                // Subsequent events use accumulated time
                accumulatedReplayTime += event.getTimeDelta();
            }

            // Update mouse position from replay data
            updateMousePosition(event.getX(), event.getY());

            // Check for key state changes
            boolean key1Pressed = (event.getKeyMask() & 1) != 0; // Bit 0 for key 1
            boolean key2Pressed = (event.getKeyMask() & 2) != 0; // Bit 1 for key 2
            keyHolded = key1Pressed || key2Pressed;
            notifyListeners(new GameEvent(GameEventType.INPUT_OVERLAY_CHANGED,
                    new InputOverlayEvent(key1Pressed, key2Pressed)));

            // Detect key press events (transition from not pressed to pressed)
            if (key1Pressed && !wasKey1Pressed) {
                keyPressed = true;
                System.out.println("Key 1 pressed at time: " + elapsedMillis);
            }
            if (key2Pressed && !wasKey2Pressed) {
                keyPressed = true;
                System.out.println("Key 2 pressed at time: " + elapsedMillis);
            }

            // Update previous key states
            wasKey1Pressed = key1Pressed;
            wasKey2Pressed = key2Pressed;

            currentReplayEventIndex++;
        }

        return keyPressed;
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
        return 1.0;
    }

    private void updateHitCount(HitObject hitObject, HitResult hitResult) {
        if (hitResult != HitResult.SPIN && hitResult != HitResult.COMPLETE_SPIN && hitResult != HitResult.SLIDER_END) {
            // System.out.println("combo naik");
            if (hitObject instanceof HitSlider) {
                if (((HitSlider) hitObject).isTailMissed()) return;
            }
            masterComboNumber++;
            updateHighestCombo(masterComboNumber);
        }

        if (hitResult == HitResult.PERFECT) {
            if (perfectCombo && hitObject.isComboEnd()) {
                gekiHits++;
            } else
                perfectHits++;
        } else if (hitResult == HitResult.GREAT) {
            if (!imperfectOrMissed && hitObject.isComboEnd()) {
                greatKatuHits++;
            } else
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

        if (hitObject instanceof HitCircle)
            hitObject.setVisible(false);
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
            hitObject.setHit(true);
            hitObject.playHitEffect();
            onComboBreak();
            return;
        }

        hitObject.setHit(true);
        hitObject.playHitEffect();

        if (!(hitObject instanceof HitCircle))
            return;
        // play sfx
        for (String sfx : hitObject.getSfxFilenames()) {
            SfxManager.playBeatmapSfx(sfx);
        }
        // Determine hit result based on timing
        if (hitResult == HitResult.MISS)
            notifyMiss(hitObject);
        else
            notifyHit(hitObject, hitResult);
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
        if (hitObject instanceof HitSpinner)
            return;
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
                new ComboChangeEvent(masterComboNumber, oldCombo >= 20)));
        notifyListeners(new GameEvent(GameEventType.HIT_OBJECT_MISSED,
                new HitObjectEvent(hitObject, HitResult.MISS,
                        false, true)));
        notifyListeners(new GameEvent(GameEventType.ACCURACY_CHANGED, accuracy));
        notifyListeners(new GameEvent(GameEventType.HEALTH_CHANGED, health));

        // Check for game over (health reaches 0)
        // if (health <= 0) {
        // System.out.println("hp reached 0, stopping game");
        // }
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
        firstHitObject = hitObjects.get(0);
        lastHpDrainMillis = firstHitObject.getHitTime();
    }

    private void createHitObject(String data, boolean comboEnd) {
        boolean isThisObjectANewCombo = HitObjectFactory.checkNewCombo(data);
        int comboSkipFromThisObject = HitObjectFactory.getComboSkipCount(data);

        if (isThisObjectANewCombo) {
            currentComboNumberInSet = 1;
            currentComboSetIndex = (currentComboSetIndex + 1 + comboSkipCounter) % OsuParser.getColours().size();
            comboSkipCounter = comboSkipFromThisObject;
        } else {
            currentComboNumberInSet++;
        }

        HitObject newHitObject = HitObjectFactory.createHitObject(data, beatmap,
                currentComboNumberInSet, currentComboSetIndex, comboEnd, this);
        hitObjects.add(newHitObject);
    }

    public ReplayManager(Beatmap beatmap, ArrayList<ReplayEvent> replayEvents,
            InputManager inputManager, CoordinateConverter coordinateConverter) {
        this.beatmap = beatmap;
        this.hitObjects = new ArrayList<>();
        this.replayEvents = replayEvents;
        this.inputManager = inputManager;
        this.coordinateConverter = coordinateConverter;
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
    public void onComboBreak() {
        int oldCombo = masterComboNumber;
        masterComboNumber = 0;

        notifyListeners(new GameEvent(GameEventType.COMBO_CHANGED,
                new ComboChangeEvent(masterComboNumber, oldCombo >= 20)));
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
