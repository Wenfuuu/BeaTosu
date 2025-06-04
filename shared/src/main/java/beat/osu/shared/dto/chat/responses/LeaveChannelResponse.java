package beat.osu.shared.dto.chat.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveChannelResponse implements Serializable {
    private String message;
}