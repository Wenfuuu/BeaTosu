package beat.osu.shared.dto.user.events;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserCountChangedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userCount;
    private long timestamp;

    public UserCountChangedEvent(int userCount) {
        this.userCount = userCount;
        this.timestamp = System.currentTimeMillis();
    }
}