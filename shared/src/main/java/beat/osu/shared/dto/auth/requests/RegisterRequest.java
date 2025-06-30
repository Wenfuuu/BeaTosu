package beat.osu.shared.dto.auth.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String email;
    private String countryCode;
    private byte[] profilePicture;
    private boolean isSupporter;
}