package beat.osu.client.view.game;

import beat.osu.client.controller.SpectateController;
import beat.osu.client.events.game.GameEvent;
import beat.osu.client.interfaces.game.GameEventListener;
import beat.osu.client.view.shared.common.Page;
import beat.osu.shared.dto.game.SpectateDto;
import javafx.stage.Stage;

public class SpectateView extends Page implements GameEventListener {

    private final SpectateController spectateController;

    public SpectateView(Stage stage, SpectateDto spectateDto, SpectateController spectateController) {
        super(stage);
        this.spectateController = spectateController;
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
