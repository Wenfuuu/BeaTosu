package beat.osu.client.connection;

import beat.osu.shared.models.RealtimeMessage;

import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RealtimeMessageHandler {
    private final ObjectOutputStream oos;
    private final Object writeLock;
    private final List<RealtimeMessageCallback> callbacks = new CopyOnWriteArrayList<>();
    private int messagesSent = 0;
    private static final int RESET_INTERVAL = 100; // Reset stream every 100 messages

    public RealtimeMessageHandler(ObjectOutputStream oos, Object writeLock) {
        this.oos = oos;
        this.writeLock = writeLock;
    }

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
            synchronized (writeLock) {
                oos.writeObject(message);
                oos.flush();

                // Reset ObjectOutputStream periodically to prevent stream corruption
                messagesSent++;
                if (messagesSent >= RESET_INTERVAL) {
                    oos.reset();
                    messagesSent = 0;
                }
            }
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
