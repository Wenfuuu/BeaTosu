package beat.osu.shared.dto.match.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetMatchByIdRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
}
