package beat.osu.client.connection;

import beat.osu.shared.models.RealtimeMessage;
import lombok.AllArgsConstructor;

import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@AllArgsConstructor
public class RealtimeMessageHandler {
    private final ObjectOutputStream oos;
    private final List<RealtimeMessageCallback> callbacks = new CopyOnWriteArrayList<>();

    public interface RealtimeMessageCallback {
        void onRealtimeMessage(RealtimeMessage message);
    }

    public void addCallback(RealtimeMessageCallback callback) {
        callbacks.add(callback);
    }

    public void removeCallback(RealtimeMessageCallback callback) {
        callbacks.remove(callback);
    }

    public void sendRealtimeMessage(RealtimeMessage message) {
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (Exception e) {
            System.err.println("Error sending realtime message: " + e.getMessage());
        }
    }

    public void handleIncomingMessage(RealtimeMessage message) {
        for (RealtimeMessageCallback callback : callbacks) {
            try {
                callback.onRealtimeMessage(message);
            } catch (Exception e) {
                System.err.println("Error in realtime message callback: " + e.getMessage());
            }
        }
    }
}
