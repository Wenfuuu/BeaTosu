package beat.osu.client.helper;

import beat.osu.client.controller.SpectateController;
import beat.osu.client.enums.GameEventType;
import beat.osu.client.enums.HealthRecover;
import beat.osu.client.enums.HitResult;
import beat.osu.client.events.game.*;
import beat.osu.client.factory.HitObjectFactory;
import beat.osu.client.interfaces.game.GameEventListener;
import beat.osu.client.interfaces.game.GameEventPublisher;
import beat.osu.client.interfaces.game.HitObjectListener;
import beat.osu.client.model.*;
import beat.osu.client.utils.OsuParser;
import beat.osu.shared.dto.game.events.SpectateEvent;
import javafx.animation.AnimationTimer;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpectateManager implements GameEventPublisher, HitObjectListener {
    private final SpectateController spectateController;
    private final List<GameEventListener> gameEventListenerList = new CopyOnWriteArrayList<>();

    private final Beatmap beatmap;
    @Getter
    private final ArrayList<HitObject> hitObjects;
    private AnimationTimer spectateLoop;
    private long startTimeNanos = -1;
    private long pauseStartNanos = -1;
    private long totalPausedNanos = 0;
//    private final long replayStartOffset = 2000;
//    private long lastHpDrainMillis = 0;
//    private ReplayState replayState = ReplayState.NOT_STARTED;
    private boolean bgmStarted = false;
    private final InputManager inputManager;

    private double currentMouseX;
    private double currentMouseY;

    // Replay event processing fields
    private long currentSpectateTime = -2000;
    private boolean wasKey1Pressed = false;
    private boolean wasKey2Pressed = false;

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
        notifyListeners(new GameEvent(GameEventType.CURSOR_MOVED, new CursorMoveEvent(currentMouseX, currentMouseY)));
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
            return;
        }

        hitObject.setHit(true);
        hitObject.playHitEffect();

        if (!(hitObject instanceof HitCircle))
            return;
        // play sfx
        for (String sfx : hitObject.getSfxFilenames()) {
            SfxManager.playSfx(sfx);
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
                new ComboChangeEvent(masterComboNumber, oldCombo > 0)));
        notifyListeners(new GameEvent(GameEventType.HIT_OBJECT_MISSED,
                new HitObjectEvent(hitObject, HitResult.MISS,
                        false, true)));
        notifyListeners(new GameEvent(GameEventType.ACCURACY_CHANGED, accuracy));
        notifyListeners(new GameEvent(GameEventType.HEALTH_CHANGED, health));

        // Check for game over (health reaches 0)
        if (health <= 0) {
            System.out.println("hp reached 0, stopping game");
            // failGame();
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

    public SpectateManager(Beatmap beatmap, SpectateController spectateController, InputManager inputManager) {
        this.beatmap = beatmap;
        this.hitObjects = new ArrayList<>();
        this.spectateController = spectateController;
        this.inputManager = inputManager;

        setupUserCallbacks();
        processBeatmap();
    }

    private void updateSpectate(SpectateEvent event) {

    }

    private void setupUserCallbacks() {
        spectateController.addSpectateEventCallback(this::updateSpectate);
    }

    @Override
    public void addListener(GameEventListener gameEventListener) {

    }

    @Override
    public void removeListener(GameEventListener gameEventListener) {

    }

    @Override
    public void notifyListeners(GameEvent event) {

    }

    @Override
    public void onHit(HitObject hitObject, HitResult result) {

    }

    @Override
    public void onMiss(HitObject hitObject) {

    }

    @Override
    public void onAdditionalSpin(HitObject hitObject, int extraSpins) {

    }

    @Override
    public void onSliderTick(HitObject hitObject) {

    }

    @Override
    public void onSliderRepeat(HitObject hitObject) {

    }

    @Override
    public void onSliderEnd(HitObject hitObject) {

    }
}
