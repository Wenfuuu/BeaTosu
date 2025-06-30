package beat.osu.shared.dto.match.requests;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMatchNameRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int matchId;
    private String newName;
}
