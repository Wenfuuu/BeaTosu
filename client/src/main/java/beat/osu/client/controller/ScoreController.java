package beat.osu.client.controller;

import beat.osu.client.service.ClientService;

public class ScoreController {
    private final ClientService clientService;

    public ScoreController() {
        this.clientService = ClientService.getInstance();
    }


}
