package app.repository;

import app.model.EmailPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailPreferenceRepository extends JpaRepository<EmailPreference, UUID> {

    Optional<EmailPreference> findByUserId(UUID userId);
}
