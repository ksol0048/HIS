package kroryi.his.domain;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Getter
@Data
@ToString
public class LoginResponse {
    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }

}