package org.youdzhin.auth.models.token;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.youdzhin.auth.models.enums.TokenType;
import org.youdzhin.auth.models.user.User;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Token {

    @Id
    @GeneratedValue
    private Long id;

    private String value;

    @Enumerated(EnumType.STRING)
    private TokenType TokenType;

    private boolean isExpired;

    private boolean isRevoked;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
