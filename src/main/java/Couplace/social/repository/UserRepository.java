package Couplace.social.repository;

import Couplace.social.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    @Query("SELECT u FROM UserEntity u WHERE u.userId = :userId")
    Optional<UserEntity> findByUserId(UUID userId);

    UserEntity findByProviderId(String providerId);
}