package beat.osu.shared.dto.match.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class TransferHostRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int matchId;
    private int newHostUserId;
}
