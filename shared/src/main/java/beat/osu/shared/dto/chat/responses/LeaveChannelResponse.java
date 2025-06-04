package beat.osu.shared.dto.chat.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveChannelResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
}