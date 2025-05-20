package beat.osu.beatosu.model;

import javafx.scene.Node;

public class HitSlider extends HitObject {



    public HitSlider(int osuX, int osuY, long hitTime, int type, int hitSound, String hitSample, double approachRate) {
        super(osuX, osuY, hitTime, type, hitSound, hitSample, approachRate);
    }

    @Override
    public Node getNode() {
        return null;
    }

    @Override
    public void update(long currentTime) {

    }

    @Override
    public void handleEvent() {

    }

    @Override
    public void setPosition(double paneX, double paneY) {

    }
}
