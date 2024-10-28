package solo.project.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import solo.project.dto.request.UserUpdateRequestDto;
import solo.project.enums.LoginType;
import solo.project.enums.UserRole;

@Entity
@Getter
@NoArgsConstructor
@Table(name="users")
public class User extends BaseTimeEntity{
    @Id
    private String uid;

    @Column(unique=true, nullable = false)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column
    private LoginType loginType;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean deleted;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean emailOtp;

    @Builder
    public User(String uid, String nickname, String email, String password, UserRole userRole,LoginType loginType ,boolean deleted , boolean emailOtp) {
        this.uid = uid;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.loginType = loginType;
        this.deleted= deleted;
        this.emailOtp = emailOtp;

    }

    public void update(UserUpdateRequestDto userDto){
        this.nickname = userDto.getNickname();
        this.userRole = userDto.getUserRole();
    }

    public void setDeleted(boolean deleted){
        this.deleted =deleted;
    }

    public void setEmailOtp(boolean emailOtp){
        this.emailOtp = emailOtp;
    }
}
