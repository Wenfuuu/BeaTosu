package beat.osu.server;

import beat.osu.server.config.ConfigurationManager;
import beat.osu.server.handler.ClientHandler;
import beat.osu.server.repositories.BeatmapRepository;
import beat.osu.server.repositories.BeatmapSetRepository;
import beat.osu.server.repositories.UserRepository;
import beat.osu.server.router.MessageRouter;
import beat.osu.server.service.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public Main() {
        ConfigurationManager config = ConfigurationManager.getInstance();

        UserRepository userRepository = new UserRepository();
        BeatmapSetRepository beatmapSetRepository = new BeatmapSetRepository();
        BeatmapRepository beatmapRepository = new BeatmapRepository();

        SessionService sessionService = new SessionService();
        SystemService systemService = new SystemService(sessionService, userRepository);

        UserService userService = new UserService(userRepository);
        AuthService authService = new AuthService(userRepository, sessionService);
        BeatmapService beatmapService = new BeatmapService(beatmapSetRepository, beatmapRepository);
        ChannelService channelService = new ChannelService(sessionService, userService);

        MessageRouter messageRouter = new MessageRouter(systemService, authService, beatmapService, channelService);

        int serverPort = config.getServerPort();

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            while (true) {
                var clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, messageRouter, sessionService, userService);
                threadPool.submit(clientHandler);
            }

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}