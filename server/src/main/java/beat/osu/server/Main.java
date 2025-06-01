package beat.osu.server;

import beat.osu.server.entities.Beatmap;
import beat.osu.server.handler.ClientHandler;
import beat.osu.server.repositories.BeatmapRepository;
import beat.osu.server.repositories.BeatmapSetRepository;
import beat.osu.server.repositories.UserRepository;
import beat.osu.server.router.MessageRouter;
import beat.osu.server.service.AuthService;
import beat.osu.server.service.BeatmapService;
import beat.osu.server.service.SessionService;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final Integer PORT = 8081;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public Main() {
        UserRepository userRepository = new UserRepository();
        BeatmapSetRepository beatmapSetRepository = new BeatmapSetRepository();
        BeatmapRepository beatmapRepository = new BeatmapRepository();

        SessionService sessionService = new SessionService();
        AuthService authService = new AuthService(userRepository, sessionService);
        BeatmapService beatmapService = new BeatmapService(beatmapSetRepository, beatmapRepository);

        MessageRouter messageRouter = new MessageRouter(authService, beatmapService);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT + "...");

            while (true) {
                var clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, messageRouter, sessionService);
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