package beat.osu.shared.dto.system.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetCurrentUserCountResponse implements Serializable {
    private Integer userCount;
}