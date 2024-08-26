package Couplace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id")
    private Long id;

    @Column(name = "users_uuid",unique = true)
    private UUID userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public User(String name, String provider, String providerId, String email, String password, LocalDateTime createdAt, LocalDateTime updatedAt, String auth) {
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }
    @Override
    public String getUsername(){
        return email;
    }
    @Override
    public String getPassword(){
        return password;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true; //만료 확인
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; //계정 잠금 확인
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true; //패스워드 만료 확인
    }
    @Override
    public boolean isEnabled() {
        return true; //계정 사용 가능 확인
    }
}