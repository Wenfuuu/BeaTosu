package beat.osu.client.view.replay;

import beat.osu.client.game.GameEndData;
import beat.osu.client.game.GameEvent;
import beat.osu.client.game.ReplayEventData;
import beat.osu.client.helper.BgmManager;
import beat.osu.client.helper.GameManager;
import beat.osu.client.helper.ReplayManager;
import beat.osu.client.interfaces.Observer;
import beat.osu.client.model.Beatmap;
import beat.osu.client.model.HitObject;
import beat.osu.client.view.shared.common.Page;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;

public class ReplayView extends Page implements Observer {
    private final double OSU_WIDTH = 640.0;
    private final double OSU_HEIGHT = 480.0;
    private final double OSU_ASPECT_RATIO = OSU_WIDTH / OSU_HEIGHT;

    private final double PLAYFIELD_OFFSET_X_IN_REF = 64.0;
    private final double PLAYFIELD_OFFSET_Y_IN_REF = 56.0;

    private final double circleSize;
    private double osuPixelDiameter;

    private StackPane root;
    private Pane replayPane;

    private Beatmap beatmap;
    private final ReplayManager rm;
    private final GameEndData gameEndData;

    public ReplayView(Stage stage, Beatmap selectedBeatmap,
                      ArrayList<ReplayEventData> replayEvents,
                      GameEndData gameEndData) {
        super(stage);
        this.beatmap = selectedBeatmap;
        this.circleSize = selectedBeatmap.getCircleSize();
        this.rm = new ReplayManager(selectedBeatmap, replayEvents, inputManager);
        this.rm.addObserver(this);
        this.gameEndData = gameEndData;

        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> updateLayout();
        root.widthProperty().addListener(resizeListener);
        root.heightProperty().addListener(resizeListener);


        updateLayout();
        BgmManager.prepareGameBgm();

        rm.startReplay();
    }

    private void updateLayout() {
        double paneWidth = root.getWidth();
        double paneHeight = root.getHeight();
        System.out.println("Pane Width: " + paneWidth);
        System.out.println("Pane Height: " + paneHeight);
        if (paneWidth <= 0 || paneHeight <= 0) {
            return;
        }

        double masterScaleFactor;
        double paneAspectRatio = paneWidth / paneHeight;

        if (paneAspectRatio > OSU_ASPECT_RATIO) {
            masterScaleFactor = paneHeight / OSU_HEIGHT;
        } else {
            masterScaleFactor = paneWidth / OSU_WIDTH;
        }

        double scaledRefScreenWidth = OSU_WIDTH * masterScaleFactor;
        double scaledRefScreenHeight = OSU_HEIGHT * masterScaleFactor;

        double viewportTopLeftX = (paneWidth - scaledRefScreenWidth) / 2.0;
        double viewportTopLeftY = (paneHeight - scaledRefScreenHeight) / 2.0;

        osuPixelDiameter = (54.4 - (4.48 * this.circleSize)) * 2.0;
        double unscaledOsuPixelRadius = osuPixelDiameter / 2.0;

        for (HitObject hitObject : rm.getHitObjects()) {
            double osuX = hitObject.getOsuX();
            double osuY = hitObject.getOsuY();

            double hitObjectX_in_RefScreen = PLAYFIELD_OFFSET_X_IN_REF + osuX;
            double hitObjectY_in_RefScreen = PLAYFIELD_OFFSET_Y_IN_REF + osuY;

            double finalObjectCenterX_onPane = viewportTopLeftX + (hitObjectX_in_RefScreen * masterScaleFactor);
            double finalObjectCenterY_onPane = viewportTopLeftY + (hitObjectY_in_RefScreen * masterScaleFactor);

            double screenScaledRadius = unscaledOsuPixelRadius * masterScaleFactor;

            hitObject.updateVisuals(finalObjectCenterX_onPane, finalObjectCenterY_onPane, screenScaledRadius);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void setLayout() {

    }

    @Override
    public void onShow() {

    }

    @Override
    public void update(GameEvent event) {

    }
}
