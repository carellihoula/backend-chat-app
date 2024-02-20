package com.carel.backendapp.token;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {
    Optional<Token> findByToken(String s);

    @Query("FROM Token t INNER JOIN User u "
    + "ON t.user.id = u.id WHERE u.email = :email AND (t.expired=false AND t.revoked=false)"
    )
    List<Token> findAllValidTokenUser(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM Token t WHERE t.expired = :expired AND t.revoked = :revoked")
    void deleteAllByExpiredAndRevoked(boolean expired, boolean revoked);
}
