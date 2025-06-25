package org.youdzhin.auth.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager manager;
    private final TokenRepository tokenRepository;
    private final UserDetailsService userDetailsService;


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

        var refreshToken = jwtService.generateRefreshToken(user);
        tokenRepository.save(tokenToSave);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
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
        var refreshToken = jwtService.generateRefreshToken(user);


        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
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


    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION); // spring http
        final String refreshToken;
        final String userEmail;
        if (header == null || !header.startsWith("Bearer ")) {
            return;
        }
        refreshToken = header.substring(7);
        userEmail = jwtService.extractEmail(refreshToken);
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                revokeAllUserTokens(user);
                var accessToken = jwtService.generateToken(user);

                tokenRepository.save(
                        Token.builder()
                        .user(user)
                        .value(accessToken)
                        .TokenType(TokenType.BEARER)
                        .isExpired(false)
                        .isRevoked(false)
                        .build()
                );
                var authResponse = AuthResponse.builder()
                        .refreshToken(refreshToken)
                        .accessToken(accessToken)
                        .build();

                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}
