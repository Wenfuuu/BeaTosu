package beat.osu.client.helper;

import beat.osu.client.controller.MatchController;
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
import beat.osu.client.view.shared.common.Toast;
import beat.osu.shared.common.Result;
import beat.osu.shared.dto.game.events.SpectateEvent;
import beat.osu.shared.dto.game.events.SpectateStatusEvent;
import beat.osu.shared.dto.match.MatchDto;
import beat.osu.shared.dto.match.MatchPlayerDto;
import beat.osu.shared.dto.match.events.MatchCompletedEvent;
import beat.osu.shared.dto.match.events.MatchScoreEvent;
import beat.osu.shared.dto.match.events.PlayerFinishedEvent;
import beat.osu.shared.dto.match.responses.LeaveMatchResponse;
import beat.osu.shared.dto.user.UserDto;
import beat.osu.shared.enums.match.MatchWinCondition;
import beat.osu.shared.enums.match.PlayerStatus;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import lombok.Getter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

public class GameManager implements GameEventPublisher, HitObjectListener {
    private final List<GameEventListener> gameEventListenerList = new CopyOnWriteArrayList<>();

    private final Beatmap beatmap;
    private final MatchDto matchDto;
    @Getter
    private final ArrayList<HitObject> hitObjects;
    private boolean isMultiplayer;
    private AnimationTimer gameLoop;
    private long startTimeNanos = -1;
    private long pauseStartNanos = -1;
    private long totalPausedNanos = 0;
    private final long gameStartOffset = 2000;
    private long lastHpDrainMillis = 0;
    private GameState gameState = GameState.NOT_STARTED;
    private boolean bgmStarted = false;
    private boolean gameOffsetCompleted = false;
    private final InputManager inputManager;
    private final ScoreController scoreController;
    private final SessionController sessionController;
    private final SpectateController spectateController;
    private final MatchController matchController;

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

    // Multiplayer score tracking
    @Getter
    private final List<MatchScoreEvent> multiplayerScores = new ArrayList<>();

    // Throttling for network events
    private long lastSpectateEventSent = 0;
    private long lastMatchScoreEventSent = 0;
    private static final long SPECTATE_EVENT_INTERVAL = 11; // Send every 17ms (~ 60 FPS)
    private static final long MATCH_SCORE_EVENT_INTERVAL = 1000; // Send every 1 second

    // Prevent thread pool exhaustion by implementing thread pool protection flags
    // private volatile boolean spectateEventInProgress = false;
    // private volatile boolean matchScoreEventInProgress = false;

    int testCount = 0;

    public void updateMousePosition(double x, double y) {
        this.currentMouseX = x;
        this.currentMouseY = y;
    }

    private void pauseAllAnimations() {
        for (HitObject hitObject : hitObjects) {
            if (hitObject.isVisible()) {
                hitObject.pauseAnimations();
            }
        }
    }

    private void resumeAllAnimations() {
        for (HitObject hitObject : hitObjects) {
            if (hitObject.isVisible()) {
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
                if (isMultiplayer) {
                    System.out.println("sending initial match score event");
                    List<MatchPlayerDto> players = matchDto.getPlayers();
                    for (MatchPlayerDto player : players) {
                        System.out.println("Processing player status: " + player.getStatus());
                        if (player.getStatus() != PlayerStatus.PLAYING) continue;
                        MatchScoreEvent event = new MatchScoreEvent(matchDto.getId(), 0,
                                0, 0, 0, player.getUser());
                        updateMatchScoreEvent(event);
                    }
                }
            } else {
                System.err.println("Failed to create session: " + response.getError().getMessage());
            }
            return null;
        });
    }

    private void removeGameSession() {
        System.out.println("Removing game session");
        UserDto user = AuthManager.getUser();
        if (user == null) {
            return;
        }

        sessionController.removePlayingBeatmapSession(user.getId()).thenApply(response -> {
            if (response.isSuccess()) {
                System.out.println("Session removed successfully: " + response.getValue().getMessage());
                // notify server that player exit/completed game, that will also send final
                // match score
                sendMatchScoreEvent();
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
        gameOffsetCompleted = false;
        startTimeNanos = -1;
        totalPausedNanos = 0;

        // Reset replay data
        replayEvents.clear();
        lastReplayEventTime = -1;

        // Reset throttling variables
        lastSpectateEventSent = 0;
        lastMatchScoreEventSent = 0;

        // Reset thread pool protection flags
        // spectateEventInProgress = false;
        // matchScoreEventInProgress = false;

        // Clear multiplayer scores for new game
        if (isMultiplayer) {
            multiplayerScores.clear();
        }

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

                if (!gameOffsetCompleted && elapsedMillis >= gameStartOffset) {
                    System.out.println("Game offset completed, notifying listeners");
                    notifyListeners(new GameEvent(GameEventType.GAME_OFFSET_COMPLETED, null));
                    gameOffsetCompleted = true;
                }

                updateGame(elapsedMillis - gameStartOffset);
            }
        };
        // create game session
        createGameSession();
        gameLoop.start();
    }

    private void pauseGame() {
        System.out.println("pausing game");

        pauseStartNanos = System.nanoTime();
        gameState = GameState.PAUSED;
        BgmManager.getInstance().pauseBgm();
        pauseAllAnimations();
        notifyListeners(new GameEvent(GameEventType.GAME_PAUSED, null));

        // notify to listeners that spectate is paused
        if (!AuthManager.isAuthenticated())
            return;
        SpectateStatusEvent event = new SpectateStatusEvent(true);
        spectateController.notifySpectatorsStatusChange(event).thenApply(response -> {
            if (response.isSuccess()) {
                System.out.println("Spectate status event sent successfully: " + response.getValue().getMessage());
            } else {
                System.err.println("Failed to send spectate status event: " + response.getError().getMessage());
            }
            return null;
        });
    }

    public void resumeGame() {
        if (gameState != GameState.PAUSED)
            return;

        // Calculate pause duration
        if (pauseStartNanos != -1) {
            totalPausedNanos += System.nanoTime() - pauseStartNanos;
            pauseStartNanos = -1;
        }

        gameState = GameState.PLAYING;
        if (bgmStarted)
            BgmManager.getInstance().resumeBgm();
        // add countdown later
        resumeAllAnimations();
        notifyListeners(new GameEvent(GameEventType.GAME_RESUMED, null));

        // notify to listeners that spectate is resumed
        SpectateStatusEvent event = new SpectateStatusEvent(false);
        spectateController.notifySpectatorsStatusChange(event).thenApply(response -> {
            if (response.isSuccess()) {
                System.out.println("Spectate status event sent successfully: " + response.getValue().getMessage());
            } else {
                System.err.println("Failed to send spectate status event: " + response.getError().getMessage());
            }
            return null;
        });
    }

    public void stopGame() {
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
        if (user == null)
            return;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formatted = now.format(formatter);
        String osrFileName = String.format("%s-%s-%s.osr",
                user.getId(), beatmap.getBeatmapId(), formatted.replace("/", "-").replace(":", "-"));
        try {
            ReplayUtils.saveReplay(replayEvents, osrFileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        insertScore(user.getId(), grade, now);
        notifySpectatorsPlayerExited();
    }

    private void exitMatch() {
        // clean up match & notify exit
        gameState = GameState.EXITED;
        gameLoop.stop();
        notifySpectatorsPlayerExited();
        try {
            Result<LeaveMatchResponse> response = matchController.leaveMatch(matchDto.getId()).get();
            if (response.isSuccess()) {
                LeaveMatchResponse leaveMatchResponse = response.getValue();
                ViewManager.getInstance().showLobbyView();
            } else {
                Toast.error("Failed to leave match: " + response.getError().getMessage()).show();
            }
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void insertScore(int id, String grade, LocalDateTime now) {
        System.out.println("Inserting score for user: " + id);
        scoreController.insertScore(beatmap.getBeatmapId(), id, score,
                highestCombo, accuracy, perfectHits, gekiHits, greatHits, greatKatuHits,
                goodHits, misses, grade, now).thenApply(response -> {
                    if (response.isSuccess()) {
                        System.out.println("Score inserted successfully: " + response.getValue().getMessage());
                        // Notify spectators (this will also remove session after notification
                        // completes)
                        notifySpectatorsPlayerExited();
                    } else {
                        System.err.println("Failed to insert score: " + response.getError().getMessage());
                    }
                    return null;
                });
    }

    public void notifySpectatorsPlayerExited() {
        System.out.println("Notifying spectators that player exited game");
        spectateController.notifySpectatorsPlayerExited().thenApply(response -> {
            if (response.isSuccess()) {
                System.out.println("Player exit event sent successfully: " + response.getValue().getMessage());
                // Remove session after notification is complete
                removeGameSession();
            } else {
                System.err.println("Failed to send player exit event: " + response.getError().getMessage());
            }
            return null;
        });
    }

    private void failGame() {
        // Notify spectators (this will also remove session after notification
        // completes)
        notifySpectatorsPlayerExited();

        System.out.println("Game failed, stopping game");
        gameState = GameState.FAILED;
        gameLoop.stop();
        BgmManager.getInstance().stopBgm();
        notifyListeners(new GameEvent(GameEventType.GAME_FAILED, null));
    }

    private void updateGame(long elapsedMillis) {
        Set<KeyCode> currentKeys = inputManager.getPressedKeys();

        storeReplayEvent(elapsedMillis, currentKeys);

        boolean pressedEsc = currentKeys.contains(KeyCode.ESCAPE) &&
                !previousKeys.contains(KeyCode.ESCAPE);

        // validate break period here
        boolean inBreakPeriod = false;
        for (BreakPeriod breakPeriod : OsuParser.getBreakPeriodsList()) {
            int startTime = breakPeriod.getStartTime();
            int endTime = breakPeriod.getEndTime();
            // System.out.println("Checking break period: " + startTime + " - " + endTime +
            // ", elapsed: " + elapsedMillis);
            if (elapsedMillis >= startTime && elapsedMillis <= endTime) {
                inBreakPeriod = true;
                if (gameState != GameState.BREAK_PERIOD) {
                    System.out.println("Entering break period");
                    gameState = GameState.BREAK_PERIOD;
                    notifyListeners(new GameEvent(GameEventType.ENTER_BREAK_PERIOD, null));
                }
                break;
            }
        }

        // Return to playing state if not in break period time
        if (!inBreakPeriod && gameState == GameState.BREAK_PERIOD) {
            System.out.println("Exiting break period, returning to playing state");
            gameState = GameState.PLAYING;
            notifyListeners(new GameEvent(GameEventType.EXIT_BREAK_PERIOD, null));
        }

        if (pressedEsc) {
            if (!isMultiplayer) {
                if (gameState == GameState.PLAYING || gameState == GameState.BREAK_PERIOD) {
                    pauseGame();
                } else if (gameState == GameState.PAUSED) {
                    resumeGame();
                }
            } else {
                // clean up and notify exit
                exitMatch();
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

        boolean keyHolded = currentKeys.contains(InputManager.getKeybind1()) ||
                currentKeys.contains(InputManager.getKeybind2());

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
                    break; // Don't handle misses for spinners

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
        ReplayEvent replayEvent = new ReplayEvent(timeDelta, currentMouseX, currentMouseY, keyMask, paneWidth,
                paneHeight);
        replayEvents.add(replayEvent);

        if (AuthManager.isAuthenticated()) {
            // Throttle spectate events to reduce network load
            if (elapsedMillis - lastSpectateEventSent >= SPECTATE_EVENT_INTERVAL) {
                sendSpectateEvent(elapsedMillis, replayEvent);
                lastSpectateEventSent = elapsedMillis;
            }
        }

        // Send match score event separately to avoid nested async calls
        if (isMultiplayer && elapsedMillis - lastMatchScoreEventSent >= MATCH_SCORE_EVENT_INTERVAL
                + (long) (Math.random() * 1000)) {
            sendMatchScoreEvent();
            lastMatchScoreEventSent = elapsedMillis;
        }

        // Update last event time for next delta calculation
        lastReplayEventTime = elapsedMillis;
    }

    private void sendSpectateEvent(long elapsedMillis, ReplayEvent replayEvent) {
        if (!AuthManager.isAuthenticated() && spectateController == null)
            return;

        // if (spectateEventInProgress) {
        // System.out.println("Skipping spectate event - previous event still in
        // progress");
        // return;
        // }

        // spectateEventInProgress = true;
        testCount++;
        System.out.println("Sending spectate event, count: " + testCount);
        try {
            SpectateEvent event = new SpectateEvent(elapsedMillis, replayEvent.getX(),
                    replayEvent.getY(), replayEvent.getKeyMask(), paneWidth, paneHeight,
                    masterComboNumber, score, accuracy, health);
            spectateController.sendSpectateEvent(event).thenApply(response -> {
                if (response.isSuccess()) {
                    System.out.println("Spectate event sent successfully: " + response.getValue().getMessage());
                    // spectateEventInProgress = false;
                } else {
                    System.err.println("Failed to send spectate event: " + response.getError().getMessage());
                }
                return null;
            }).exceptionally(throwable -> {
                System.err.println("Exception in sendSpectateEvent: " + throwable.getMessage());
                throwable.printStackTrace();
                return null;
            });
        } catch (Exception e) {
            System.err.println("Error creating spectate event: " + e.getMessage());
            e.printStackTrace();
            // spectateEventInProgress = false;
        }
    }

    private void sendMatchScoreEvent() {
        if (!isMultiplayer || matchController == null)
            return;

        // if (matchScoreEventInProgress) {
        // System.out.println("Skipping match score event - previous event still in
        // progress");
        // return;
        // }

        // matchScoreEventInProgress = true;
        try {
            MatchScoreEvent event = new MatchScoreEvent(matchDto.getId(),
                    score, masterComboNumber, highestCombo, accuracy, AuthManager.getUser());
            matchController.sendMatchScoreEvent(event).thenApply(response -> {
                if (response.isSuccess()) {
                    System.out.println("Match score event sent successfully: " + response.getValue().getMessage());
                    // matchScoreEventInProgress = false;
                    if (isMultiplayer && (gameState == GameState.COMPLETED ||
                            gameState == GameState.EXITED))
                        sendPlayerFinishedEvent();
                } else {
                    System.err.println("Failed to send match score event: " + response.getError().getMessage());
                }
                return null;
            }).exceptionally(throwable -> {
                System.err.println("Exception in sendMatchScoreEvent: " + throwable.getMessage());
                throwable.printStackTrace();
                return null;
            });
        } catch (Exception e) {
            System.err.println("Error creating match score event: " + e.getMessage());
            e.printStackTrace();
            // matchScoreEventInProgress = false;
        }
    }

    private void sendPlayerFinishedEvent() {
        if (!isMultiplayer)
            return;

        try {
            System.out.println("Sending player finished event");
            PlayerFinishedEvent event = new PlayerFinishedEvent(matchDto.getId(), AuthManager.getUser());
            matchController.sendPlayerFinishedEvent(event).thenApply(response -> {
                if (response.isSuccess()) {
                    System.out.println("Player finished event sent successfully: " + response.getValue().getMessage());
                } else {
                    System.err.println("Failed to send player finished event: " + response.getError().getMessage());
                }
                return null;
            }).exceptionally(throwable -> {
                System.err.println("Exception in sendPlayerFinishedEvent: " + throwable.getMessage());
                return null;
            });
        } catch (Exception e) {
            System.err.println("Error creating player finished event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean checkHitObjectClick(HitObject hitObject, long elapsedMillis) {
        double objCenterX = hitObject.getScreenCenterX();
        double objCenterY = hitObject.getScreenCenterY();
        double objRadius = hitObject.getScreenRadius();
        if (hitObject instanceof HitSpinner)
            objRadius *= 20.0;

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

            System.out.println("index data " + i);
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
            currentComboSetIndex = (currentComboSetIndex + 1 + comboSkipCounter);
            if (!OsuParser.getColours().isEmpty())
                currentComboSetIndex %= OsuParser.getColours().size();
            comboSkipCounter = comboSkipFromThisObject; // Store skip for NEXT new combo
        } else {
            currentComboNumberInSet++;
        }

        HitObject newHitObject = HitObjectFactory.createHitObject(data, beatmap,
                currentComboNumberInSet, currentComboSetIndex, comboEnd, this);
        hitObjects.add(newHitObject);
    }

    public GameManager(Beatmap beatmap, InputManager inputManager, double paneWidth, double paneHeight,
            boolean isMultiplayer) {
        this.beatmap = beatmap;
        this.inputManager = inputManager;
        this.paneWidth = paneWidth;
        this.paneHeight = paneHeight;
        this.isMultiplayer = isMultiplayer;
        this.hitObjects = new ArrayList<>();
        this.scoreController = new ScoreController();
        this.sessionController = new SessionController();
        this.spectateController = new SpectateController();
        this.matchController = new MatchController();
        this.matchDto = ViewManager.getInstance().getCurrentMatchDto();

        processBeatmap();
        setupMatchCallbacks();
    }

    private void setupMatchCallbacks() {
        matchController.addMatchScoreCallback(this::updateMatchScoreEvent);
        matchController.addMatchCompletedCallback(this::onMatchCompletedEvent);
    }

    private void onMatchCompletedEvent(MatchCompletedEvent event) {
        System.out.println("Match completed, notifying view");
        if (gameState == GameState.EXITED)
            return;
        notifyListeners(new GameEvent(GameEventType.MATCH_COMPLETED, multiplayerScores));
    }

    private void updateMatchScoreEvent(MatchScoreEvent event) {
        try {
            if (event == null || event.getUser() == null) {
                System.err.println("Received null match score event or user");
                return;
            }

            System.out.println("Received match score, user: " + event.getUser().getUsername() +
                    ", score: " + event.getScore() + ", combo: " + event.getCombo());

            // Check if multiplayer components are still valid
            if (matchDto == null) {
                System.err.println("Match DTO are null, ignoring match score event");
                return;
            }

            // Find and update existing score for this user, or add new entry
            boolean found = false;
            for (int i = 0; i < multiplayerScores.size(); i++) {
                MatchScoreEvent existingEvent = multiplayerScores.get(i);
                if (existingEvent != null && existingEvent.getUser() != null &&
                        existingEvent.getUser().getId() == event.getUser().getId()) {
                    if (event.getScore() != 0)
                        multiplayerScores.set(i, event);
                    found = true;
                    break;
                }
            }
            if (!found)
                multiplayerScores.add(event);

            if (matchDto.getWinCondition() == MatchWinCondition.SCORE) {
                multiplayerScores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
            } else if (matchDto.getWinCondition() == MatchWinCondition.COMBO) {
                multiplayerScores.sort((a, b) -> Integer.compare(b.getHighestCombo(), a.getHighestCombo()));
            } else if (matchDto.getWinCondition() == MatchWinCondition.ACCURACY) {
                multiplayerScores.sort((a, b) -> Double.compare(b.getAccuracy(), a.getAccuracy()));
            }

            notifyListeners(new GameEvent(GameEventType.MATCH_SCORE_CHANGED, multiplayerScores));
        } catch (Exception e) {
            System.err.println("Error processing match score event: " + e.getMessage());
            e.printStackTrace();
        }
        // System.out.println("Updated multiplayer scores. Current leaderboard:");
        // for (int i = 0; i < multiplayerScores.size(); i++) {
        // MatchScoreEvent score = multiplayerScores.get(i);
        // System.out.println((i + 1) + ". " + score.getUser().getUsername() +
        // " - Score: " + score.getScore() +
        // " - Combo: " + score.getCombo());
        // }
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
