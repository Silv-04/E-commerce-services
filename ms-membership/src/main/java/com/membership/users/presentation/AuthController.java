package com.membership.users.presentation;

import com.membership.users.application.dto.LoginRequest;
import com.membership.users.application.dto.LoginResponse;
import com.membership.users.application.dto.RegisterRequest;
import com.membership.users.application.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Enregistrer un nouvel utilisateur
     * @param request contenant email, password, firstName, lastName
     * @return Réponse de création
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(
            request.getEmail(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authentifier un utilisateur et retourner un JWT token
     * @param request contenant email et password
     * @return JWT token avec les informations utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.authenticate(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Vérifier la validité du token
     * @param token JWT token
     * @return status 200 si valide
     */
    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String token) {
        authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok().build();
    }
}
