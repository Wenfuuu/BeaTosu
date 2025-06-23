package beat.osu.server.service;

public class SpectateService {

    private final SessionService sessionService;
    private final UserService userService;

    public SpectateService(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    
}
