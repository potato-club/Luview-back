package solo.project.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import solo.project.dto.request.UserUpdateRequestDto;
import solo.project.enums.LoginType;
import solo.project.enums.UserRole;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name="users")
public class User extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(unique=true, nullable = false)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false) //
    private String password;

    @Column(nullable = false)
    private LocalDate birthDate;

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
    public User(Long id,String nickname, String email, String password, UserRole userRole,LocalDate birthDate,LoginType loginType ,boolean deleted , boolean emailOtp) {
        this.id=id;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.birthDate = birthDate;
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
