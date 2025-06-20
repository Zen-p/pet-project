package org.youdzhin.auth.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.youdzhin.auth.dto.AuthResponse;
import org.youdzhin.auth.dto.AuthRequest;
import org.youdzhin.auth.dto.RegisterRequest;
import org.youdzhin.auth.services.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register (@RequestBody RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate (@RequestBody AuthRequest request){
        return ResponseEntity.ok(authService.authenticate(request));
    }


}
