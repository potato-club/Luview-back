package Couplace.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddUserRequest {
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String birthdate;
}
