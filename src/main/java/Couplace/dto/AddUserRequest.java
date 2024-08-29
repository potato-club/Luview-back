package Couplace.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddUserRequest {
    private String email;
    private String password;
    private String name; // name 필드 추가
}
