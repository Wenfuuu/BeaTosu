package beat.osu.server.handler;

import beat.osu.server.model.Message;
import beat.osu.server.model.User;
import lombok.AllArgsConstructor;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@AllArgsConstructor
public class ClientHandler implements Runnable {
    private Socket clientSocket;

    @Override
    public void run() {
        try(
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
        ) {
            System.out.println("Client handler started for: " + clientSocket.getInetAddress().getHostAddress());

            Object receivedObject;

            while ((receivedObject = ois.readObject()) != null) {
                if (receivedObject instanceof Message) {
                    Message clientMessage = (Message) receivedObject;
                    System.out.println("Server received from [" + clientSocket.getRemoteSocketAddress() + "]: " + clientMessage.getText());

                    if ("CLIENT_EXITING_GRACEFULLY".equals(clientMessage.getText())) {
                        System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " requested disconnect.");
                        oos.writeObject("Server Acknowledging disconnect. Goodbye!");
                        oos.flush();
                        break;
                    }

                    String serverResponse = "Server received: '" + clientMessage.getText() + "'";
                    oos.writeObject(serverResponse);
                    oos.flush();
                    System.out.println("Server sent to [" + clientSocket.getRemoteSocketAddress() + "]: " + serverResponse);
                } else {
                    System.err.println("Server received an unexpected object type from [" + clientSocket.getRemoteSocketAddress() + "]: " + receivedObject.getClass().getName());
                    oos.writeObject("Error: Unexpected object type received by server.");
                    oos.flush();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
