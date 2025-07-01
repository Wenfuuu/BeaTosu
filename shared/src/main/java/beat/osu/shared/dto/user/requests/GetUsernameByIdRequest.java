package beat.osu.shared.dto.user.requests;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetUsernameByIdRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
}
