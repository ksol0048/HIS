package kroryi.his.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LoginRequest {
    private String mid;
    private String password;

    // Getters and setters
}
