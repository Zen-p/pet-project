package org.youdzhin.auth.services;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.youdzhin.auth.config.JwtService;
import org.youdzhin.auth.dto.AuthRequest;
import org.youdzhin.auth.dto.AuthResponse;
import org.youdzhin.auth.dto.RegisterRequest;
import org.youdzhin.auth.interfaces.TokenRepository;
import org.youdzhin.auth.interfaces.UserRepository;
import org.youdzhin.auth.models.enums.TokenType;
import org.youdzhin.auth.models.token.Token;
import org.youdzhin.auth.models.user.User;
import org.youdzhin.auth.models.enums.Roles;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager manager;
    private final TokenRepository tokenRepository;


    public AuthResponse register (RegisterRequest request) {

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Roles.USER)
                .build();

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);

        var tokenToSave = Token.builder()
                .user(user)
                .value(jwtToken)
                .TokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoked(false)
                .build();
        revokeAllUserTokens(user);
        tokenRepository.save(tokenToSave);

        return AuthResponse.builder()
                .token(jwtToken)
                .build();

    }

    public AuthResponse authenticate (AuthRequest request) {

        manager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UsernameNotFoundException("sth gone wrong"));
        var jwtToken = jwtService.generateToken(user);

        var tokenToSave = Token.builder()
                .user(user)
                .value(jwtToken)
                .TokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoked(false)
                .build();
        revokeAllUserTokens(user);
        tokenRepository.save(tokenToSave);

        return AuthResponse.builder()
                .token(jwtToken)
                .build();

    }

    public void revokeAllUserTokens (User user) {
        var validUserTokens = tokenRepository.findValidTokenByUserId(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(t -> {
            t.setRevoked(true);
            t.setExpired(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }


}
