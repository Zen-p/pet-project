package org.youdzhin.auth.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.youdzhin.auth.models.token.Token;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {


    @Query("""
    select t from Token t inner join User u on t.user.id = u.id
    where u.id = :userId and (t.isExpired = false and t.isRevoked = false)
    """)
    List<Token> findValidTokenByUserId (@Param("userId") Long userId);

    Optional<Token> findByValue(String value);

}
