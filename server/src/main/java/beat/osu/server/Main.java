package beat.osu.server;

import beat.osu.server.handler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    private final Integer PORT = 8081;

    public Main() {
        System.out.println("Starting server...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                var clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientTask = new ClientHandler(clientSocket);
                new Thread(clientTask).start();
            }

        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}