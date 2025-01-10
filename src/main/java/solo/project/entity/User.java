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
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 이거 사용시 파라미터가 있는 생성자나 빌터 패턴으로만 객체 생성할 수 있음
@Table(name="users")                                // 기본 생성자를 생성하되, 접근 수준을 PROTECTED로 제한하는 Lombok 어노테이션
public class User extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(nullable = true)
    private String name;

    @Column(unique=true, nullable = false)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private LoginType loginType;

    @Column(nullable = false, unique = true, length = 12)
    private String uniqueCode;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean deleted;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean emailOtp;

    // 아래 필드는 유저에 대한 모든 "사진, 즐겨찾기, 리뷰"들을 저장하는 리스트입니다.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorites> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @Builder
    public User(Long id, String name, String nickname, String email, String password, LocalDate birthDate, LoginType loginType, boolean deleted, boolean emailOtp) {
        this.id=id;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password != null ? password : ""; // 비밀번호가 없을 경우 빈 문자열로 설정
        this.birthDate = birthDate ; // 카카오에서 생년월일 지급되지 않을 경우 기본값 설정
        this.loginType = loginType != null ? loginType : LoginType.KAKAO;
        this.uniqueCode = generateUniqueCode();
        this.deleted = deleted;
        this.emailOtp = emailOtp;
    } //나중에 추가 정보를 받게 된다면 코드 수정 예정

    public void update(UserUpdateRequestDto userDto){
        this.nickname = userDto.getNickname();
    }

    public void setDeleted(boolean deleted){
        this.deleted =deleted;
    }

    public void setEmailOtp(boolean emailOtp){
        this.emailOtp = emailOtp;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setBirthDate(LocalDate birthDate){
        this.birthDate = birthDate;
    }

    public boolean hasAdditionalInfo(){
        return (this.name !=null && this.birthDate != null);
    }

    private String generateUniqueCode() {
        return UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private void addFile(File file){
        files.add(file);
        file.setUser(this);
    }
}
