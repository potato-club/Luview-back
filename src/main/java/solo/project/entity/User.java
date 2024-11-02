package solo.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import solo.project.dto.User.request.UserUpdateRequestDto;
import solo.project.enums.LoginType;
import solo.project.enums.UserRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 이거 사용시 파라미터가 있는 생성자나 빌터 패턴으로만 객체 생성할 수 있음
@Table(name="users")                                // 기본 생성자를 생성하되, 접근 수준을 PROTECTED로 제한하는 Lombok 어노테이션
public class User extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(nullable = false)
    private String name;

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

    // 아래 필드는 유저에 대한 모든 "사진, 즐겨찾기"들을 저장하는 리스트입니다.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorites> favorites = new ArrayList<>();

    @Builder
    public User(Long id,String name, String nickname, String email, String password, UserRole userRole,LocalDate birthDate,LoginType loginType ,boolean deleted , boolean emailOtp) {
        this.id=id;
        this.name=name;
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
