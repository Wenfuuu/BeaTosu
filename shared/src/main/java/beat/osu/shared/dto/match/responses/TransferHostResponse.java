package beat.osu.shared.dto.match.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class TransferHostResponse implements Serializable {
    private String message;
}
