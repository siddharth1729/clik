package in.siddharthsabron.clik.repositories;

import in.siddharthsabron.clik.models.authentications.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}