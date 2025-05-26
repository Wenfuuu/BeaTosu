package beat.osu.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private byte[] profilePicture;
    private int performance;
    private double accuracy;
    private int playCount;
    private int level;
}
