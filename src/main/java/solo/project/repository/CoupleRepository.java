package solo.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import solo.project.entity.Couple;
import solo.project.entity.User;

import java.util.Optional;

@Repository
public interface CoupleRepository extends JpaRepository<Couple, Long> {
  Optional<Couple> findByUser1OrUser2(User user1, User user2);
  Boolean existsByUser1AndUser2(User user1, User user2);
  Boolean existsByUser1(User user);
}
