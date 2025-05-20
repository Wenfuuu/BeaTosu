package beat.osu.server;

import beat.osu.server.model.User;

public class Main {
    public Main() {
        System.out.println("Starting server...");
        User user = new User(1L, "testUser");
        System.out.println("User created: " + user.toString());
    }

    public static void main(String[] args) {
        new Main();
    }
}