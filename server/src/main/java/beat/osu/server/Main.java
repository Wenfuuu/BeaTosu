package beat.osu.server;

import beat.osu.server.handler.ClientHandler;
import beat.osu.server.repositories.UserRepository;
import beat.osu.server.router.MessageRouter;
import beat.osu.server.service.AuthService;
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

        SessionService sessionService = new SessionService();
        AuthService authService = new AuthService(userRepository, sessionService);

        MessageRouter messageRouter = new MessageRouter(authService);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT + "...");

            while (true) {
                var clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, messageRouter, sessionService);
                threadPool.submit(clientHandler);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}