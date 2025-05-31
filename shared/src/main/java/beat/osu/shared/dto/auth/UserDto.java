package beat.osu.shared.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private int id;
    private String username;
    private String email;
    private String countryCode;
    private byte[] profilePicture;
    private int performance;
    private double accuracy;
    private int playCount;
    private int level;
}
