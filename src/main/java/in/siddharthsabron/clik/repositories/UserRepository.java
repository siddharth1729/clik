package in.siddharthsabron.clik.repositories;

import in.siddharthsabron.clik.models.authentications.User;
import in.siddharthsabron.clik.models.links.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT s FROM ShortUrl s JOIN s.user u WHERE u.email = :email")
    List<ShortUrl> findAllShortUrlsByUserEmail(@Param("email") String email);
}