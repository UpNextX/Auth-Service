package org.upnext.authservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upnext.authservice.models.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findAllByIsConfirmed(Boolean isConfirmed);
}
