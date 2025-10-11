package org.upnext.authservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upnext.authservice.enums.TokenType;
import org.upnext.authservice.models.Token;

import java.util.Optional;

public interface TokensRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenAndType(String token, TokenType type);
}
