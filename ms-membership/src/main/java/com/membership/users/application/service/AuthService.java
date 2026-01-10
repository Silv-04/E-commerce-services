package com.membership.users.application.service;

import com.membership.users.application.dto.LoginResponse;
import com.membership.users.domain.entity.User;
import com.membership.users.infrastructure.repository.UserRepository;
import com.membership.users.infrastructure.security.JwtTokenGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenGenerator jwtTokenGenerator;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenGenerator jwtTokenGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenGenerator = jwtTokenGenerator;
    }

    /**
     * Enregistrer un nouvel utilisateur
     * @param email email de l'utilisateur
     * @param password password en clair
     * @param firstName prénom
     * @param lastName nom
     * @return LoginResponse avec JWT token
     */
    public LoginResponse register(String email, String password, String firstName, String lastName) {
        // Vérifier que l'email n'existe pas déjà
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé: " + email);
        }

        // Créer le nouvel utilisateur
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setUsername(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRoles(Arrays.asList("USER"));
        user.setActive(true);

        // Sauvegarder l'utilisateur
        user = userRepository.save(user);

        // Générer le JWT token
        String token = jwtTokenGenerator.generateToken(user);

        // Retourner la réponse
        return new LoginResponse(
            token,
            user.getId(),
            user.getEmail(),
            user.getUsername()
        );
    }

    /**
     * Authentifier un utilisateur avec email et password
     * @param email email de l'utilisateur
     * @param password password en clair
     * @return LoginResponse avec JWT token
     * @throws RuntimeException si utilisateur non trouvé ou password incorrect
     */
    public LoginResponse authenticate(String email, String password) {
        // Chercher l'utilisateur par email
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));

        // Vérifier que l'utilisateur est actif
        if (!user.getActive()) {
            throw new RuntimeException("Utilisateur inactif: " + email);
        }

        // Vérifier le password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Password incorrect pour: " + email);
        }

        // Générer le JWT token
        String token = jwtTokenGenerator.generateToken(user);

        // Retourner la réponse
        return new LoginResponse(
            token,
            user.getId(),
            user.getEmail(),
            user.getUsername()
        );
    }

    /**
     * Valider un JWT token
     * @param token JWT token
     * @throws RuntimeException si token invalide
     */
    public void validateToken(String token) {
        jwtTokenGenerator.validateToken(token);
    }
}
