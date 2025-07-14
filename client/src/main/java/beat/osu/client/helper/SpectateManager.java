package beat.osu.client.helper;

import beat.osu.client.controller.SpectateController;
import beat.osu.client.enums.GameEventType;
import beat.osu.client.enums.GameState;
import beat.osu.client.enums.HitResult;
import beat.osu.client.events.game.*;
import beat.osu.client.factory.HitObjectFactory;
import beat.osu.client.interfaces.game.CoordinateConverter;
import beat.osu.client.interfaces.game.GameEventListener;
import beat.osu.client.interfaces.game.GameEventPublisher;
import beat.osu.client.interfaces.game.HitObjectListener;
import beat.osu.client.model.*;
import beat.osu.client.utils.OsuParser;
import beat.osu.shared.dto.game.SpectateDto;
import beat.osu.shared.dto.game.events.SpectateEvent;
import beat.osu.shared.dto.game.events.SpectateStatusEvent;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpectateManager implements GameEventPublisher, HitObjectListener {
    private final SpectateController spectateController;
    private final List<GameEventListener> gameEventListenerList = new CopyOnWriteArrayList<>();

    private final Beatmap beatmap;
    @Getter
    private final ArrayList<HitObject> hitObjects;
    // private boolean bgmStarted = false;
    private GameState gameState = GameState.PLAYING;
    private final InputManager inputManager;
    private final CoordinateConverter coordinateConverter;
    private AnimationTimer spectateLoop;

    private double currentMouseX;
    private double currentMouseY;

    // Replay event processing fields
    private boolean wasKey1Pressed = false;
    private boolean wasKey2Pressed = false;
    private boolean keyHolded = false;
    private final long gameStartOffset = 2000;
    private boolean spectateOffsetCompleted = false;

    private int masterComboNumber = 0;
    private int currentComboNumberInSet = 0;
    private int currentComboSetIndex = 0;
    private int comboSkipCounter = 0;

    private int score = 0;
    private double accuracy = 100.0;
    private double health = 100;
    private boolean perfectCombo = true;
    private boolean imperfectOrMissed = false;
    private boolean isPreExit = false;
    private boolean isHalfBreakperiod = false;

    private boolean firstSpectateEvent = true;
    private volatile boolean spectateStoppingFlag = false;

    private void updateMousePosition(double x, double y) {
        this.currentMouseX = coordinateConverter.convertReplayMouseX(x);
        this.currentMouseY = coordinateConverter.convertReplayMouseY(y);

        Platform.runLater(() -> {
            notifyListeners(new GameEvent(GameEventType.CURSOR_MOVED,
                    new CursorMoveEvent(currentMouseX, currentMouseY)));
        });
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

    private void notifyHit(HitObject hitObject, HitResult hitResult) {
        notifyListeners(new GameEvent(GameEventType.HIT_OBJECT_HIT,
                new HitObjectEvent(hitObject, hitResult, perfectCombo, imperfectOrMissed)));
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

        notifyListeners(new GameEvent(GameEventType.HIT_OBJECT_MISSED,
                new HitObjectEvent(hitObject, HitResult.MISS, false, true)));

        // Check for game over (health reaches 0)
        // if (health <= 0) {
        // System.out.println("hp reached 0, stopping game");
        // failGame();
        // }
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

    public SpectateManager(Beatmap beatmap, SpectateDto spectateDto, SpectateController spectateController,
            InputManager inputManager, CoordinateConverter coordinateConverter) {
        this.beatmap = beatmap;
        this.hitObjects = new ArrayList<>();
        this.spectateController = spectateController;
        this.inputManager = inputManager;
        this.coordinateConverter = coordinateConverter;

        setupSpectateCallbacks();
        processBeatmap();

        startSpectate(spectateDto);
    }

    private void resetSpectateState() {
        // Reset all game state variables
        masterComboNumber = 0;
        currentComboNumberInSet = 0;
        currentComboSetIndex = 0;
        comboSkipCounter = 0;

        score = 0;
        accuracy = 100.0;
        health = 100;
        perfectCombo = true;
        imperfectOrMissed = false;

        firstSpectateEvent = true;
        wasKey1Pressed = false;
        wasKey2Pressed = false;
        spectateStoppingFlag = false;

        currentMouseX = 0;
        currentMouseY = 0;
    }

    public void startSpectate(SpectateDto spectateDto) {
        System.out.println("Starting spectate session - clearing input state");

        resetSpectateState();
        inputManager.getPressedKeys().clear();
        System.out.println("Cleared input manager key states");

        spectateController.startSpectate(spectateDto).thenApply(response -> {
            if (response.isSuccess()) {
                System.out.println("Successfully start spectating: " + response.getValue().getMessage());
            } else {
                System.err.println("Failed to start spectating: " + response.getError().getMessage());
            }
            return null;
        });

        wasKey1Pressed = false;
        wasKey2Pressed = false;
        spectateOffsetCompleted = false;

        spectateLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (spectateStoppingFlag) {
                    System.out.println("Spectate loop stopped due to stopping flag");
                    stop();
                    return;
                }
                Set<KeyCode> currentKeys = inputManager.getPressedKeys();
                boolean pressedEsc = currentKeys.contains(KeyCode.ESCAPE);

                if (pressedEsc) {
                    notifyListeners(new GameEvent(GameEventType.SPECTATE_EXIT, null));
                }
            }
        };
        spectateLoop.start();
    }

    public void stopSpectate() {
        spectateStoppingFlag = true;

        spectateController.stopSpectate().thenApply(response -> {
            if (response.isSuccess()) {
                System.out.println("Successfully stopped spectating: " + response.getValue().getMessage());

                // Use Platform.runLater to ensure UI cleanup happens on JavaFX Application
                // Thread
                Platform.runLater(() -> {
                    try {
                        cleanupSpectateResources();
                        ViewManager.getInstance().goToPreviousPage();
                    } catch (Exception e) {
                        System.err.println("Error during spectate cleanup: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } else {
                System.err.println("Failed to stop spectating: " + response.getError().getMessage());
                spectateStoppingFlag = false;
            }
            return null;
        });
    }

    private void cleanupSpectateResources() {
        try {
            if (BgmManager.getInstance().getCurrentPlayer() != null) {
                BgmManager.getInstance().getCurrentPlayer().stop();
            }

            for (HitObject hitObject : hitObjects) {
                if (hitObject != null) {
                    hitObject.pauseAnimations();
                    hitObject.setVisible(false);
                }
            }

            // Clear the hit objects list
            hitObjects.clear();

            firstSpectateEvent = true;
            wasKey1Pressed = false;
            wasKey2Pressed = false;

            // Clear input manager state
            inputManager.getPressedKeys().clear();

            System.out.println("Spectate resources cleaned up successfully");
        } catch (Exception e) {
            System.err.println("Error during spectate resource cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateSpectate(SpectateEvent event) {
        // Early exit if spectate is stopping
        if (spectateStoppingFlag) {
            System.out.println("Ignoring spectate event - spectate session is stopping");
            return;
        }

        System.out.println("Received spectate event: " + event);
        Platform.runLater(() -> {
            if (spectateStoppingFlag) {
                System.out.println("Ignoring spectate event in Platform.runLater - spectate session is stopping");
                return;
            }

            if (hitObjects == null || (hitObjects.isEmpty() && !firstSpectateEvent)) {
                System.out.println("Ignoring spectate event - session appears to be stopped");
                return;
            }

            if (firstSpectateEvent) {
                firstSpectateEvent = false;
                // clear all hit objects that has hit time before the first event
                hitObjects.removeIf(hitObject -> hitObject.getHitTime() < event.getCurrentTime());
                if (event.getCurrentTime() <= 0)
                    return;
                BgmManager.getInstance().getCurrentPlayer().seek(Duration.millis(event.getCurrentTime()));
                BgmManager.getInstance().playGameBgm();
            } else {
                if (event.getCurrentTime() <= 0)
                    return;
                System.out.println("Current time: " + event.getCurrentTime());
                System.out.println("Current BGM time: " + BgmManager.getInstance().getCurrentPlayer().getCurrentTime());
                // seek bgm duration to the current time of the event if has difference more
                // than 50ms
                Duration currentBgmTime = BgmManager.getInstance().getCurrentPlayer().getCurrentTime();
                if (Math.abs(currentBgmTime.toMillis() - event.getCurrentTime()) > 50) {
                    BgmManager.getInstance().getCurrentPlayer().seek(Duration.millis(event.getCurrentTime()));
                    System.out.println("Seeking BGM to: " + event.getCurrentTime());
                }
            }

            Set<KeyCode> currentKeys = inputManager.getPressedKeys();
            boolean pressedEsc = currentKeys.contains(KeyCode.ESCAPE);

            boolean inBreakPeriod = false;
            for (BreakPeriod breakPeriod : OsuParser.getBreakPeriodsList()) {
                int startTime = breakPeriod.getStartTime();
                int endTime = breakPeriod.getEndTime();
                if (event.getCurrentTime() >= startTime && event.getCurrentTime() <= endTime) {
                    inBreakPeriod = true;
                    if (gameState != GameState.BREAK_PERIOD) {
                        System.out.println("Entering break period");
                        gameState = GameState.BREAK_PERIOD;
                        notifyListeners(new GameEvent(GameEventType.ENTER_BREAK_PERIOD, null));
                    } else {
                        int totalBreakTime = endTime - startTime;
                        // check if elapsedMillis has passed half of the break period
                        if (totalBreakTime >= 3000 && event.getCurrentTime() >= startTime + totalBreakTime / 2) {
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

                        if (event.getCurrentTime() + 1000 >= endTime) {
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

            if (!spectateOffsetCompleted && event.getCurrentTime() >= gameStartOffset) {
                System.out.println("Game offset completed, notifying listeners");
                notifyListeners(new GameEvent(GameEventType.GAME_OFFSET_COMPLETED, null));
                spectateOffsetCompleted = true;
            }

            if (!inBreakPeriod && gameState == GameState.BREAK_PERIOD) {
                System.out.println("Exiting break period, returning to playing state");
                isHalfBreakperiod = false;
                isPreExit = false;
                gameState = GameState.PLAYING;
                notifyListeners(new GameEvent(GameEventType.EXIT_BREAK_PERIOD, null));
            }

            if (pressedEsc) {
                System.out.println("Escape key pressed, stopping spectate session");
                stopSpectate();
                return;
            }

            long elapsedMillis = event.getCurrentTime();
            boolean keyPressed = processSpectateEvents(event);
            int oldCombo = masterComboNumber;
            masterComboNumber = event.getCombo();
            score = event.getScore();
            accuracy = event.getAccuracy();
            health = event.getHealth();

            notifyListeners(new GameEvent(GameEventType.SCORE_CHANGED, score));
            notifyListeners(new GameEvent(GameEventType.COMBO_CHANGED,
                    new ComboChangeEvent(masterComboNumber, (oldCombo >= 20 && masterComboNumber < oldCombo))));
            notifyListeners(new GameEvent(GameEventType.ACCURACY_CHANGED, accuracy));
            notifyListeners(new GameEvent(GameEventType.HEALTH_CHANGED, health));

            Iterator<HitObject> iterator = hitObjects.iterator();
            while (iterator.hasNext()) {
                HitObject hitObject = iterator.next();
                hitObject.update(elapsedMillis);
                if (hitObject instanceof HitSpinner) {
                    if (keyHolded)
                        ((HitSpinner) hitObject).updateSpinner(currentMouseX, currentMouseY);
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

            if (hitObjects.isEmpty() && !firstSpectateEvent) {
                System.out.println("All hit objects processed, spectate session ending naturally");
                stopSpectate();
            }
        });
    }

    private boolean processSpectateEvents(SpectateEvent event) {
        boolean keyPressed = false;

        updateMousePosition(event.getX(), event.getY());
        boolean key1Pressed = (event.getKeyMask() & 1) != 0; // Bit 0 for key 1
        boolean key2Pressed = (event.getKeyMask() & 2) != 0; // Bit 1 for key 2
        keyHolded = key1Pressed || key2Pressed;
        notifyListeners(new GameEvent(GameEventType.INPUT_OVERLAY_CHANGED,
                new InputOverlayEvent(key1Pressed, key2Pressed)));

        if (key1Pressed && !wasKey1Pressed) {
            keyPressed = true;
            System.out.println("Key 1 pressed at time: " + event.getCurrentTime());
        }
        if (key2Pressed && !wasKey2Pressed) {
            keyPressed = true;
            System.out.println("Key 2 pressed at time: " + event.getCurrentTime());
        }

        // Update previous key states
        wasKey1Pressed = key1Pressed;
        wasKey2Pressed = key2Pressed;

        return keyPressed;
    }

    private void updateSpectateStatus(SpectateStatusEvent event) {
        System.out.println("Received spectate status event, spectate pause status: " + event.isPaused());
        if (event.isPaused())
            pauseAllAnimations();
        else
            resumeAllAnimations();
    }

    private void setupSpectateCallbacks() {
        spectateController.addSpectateEventCallback(this::updateSpectate);
        spectateController.addSpectateStatusEventCallback(this::updateSpectateStatus);
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
