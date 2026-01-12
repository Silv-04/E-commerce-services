package com.episen.ms_product.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.episen.ms_product.domain.entity.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;

/**
 * Filtre d'authentification JWT pour sécuriser les endpoints de l'application.
 */
@Component
public class JwtAuthentificationFilter extends OncePerRequestFilter {
    private final JwtTokenValidator jwtTokenValidator;

    public JwtAuthentificationFilter(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }

    /**
     * Filtre les requêtes HTTP pour valider le token JWT dans l'en-tête Authorization.
     * @param request  La requête HTTP entrante.
     * @param response La réponse HTTP sortante.
     * @param filterChain La chaîne de filtres.
     * @throws IOException En cas d'erreur d'entrée/sortie.
     * @throws ServletException En cas d'erreur de servlet.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws IOException, ServletException {

        if (request.getRequestURI().startsWith("/actuator") ||
            request.getRequestURI().startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            System.out.println("Missing or invalid Authorization header");
            return;
        }

        String token = header.substring(7);
        System.out.println("Token received: " + token);

        try {
            User user = jwtTokenValidator.transform(token);
            System.out.println("Authenticated user: " + user.toString());
            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> (GrantedAuthority) () -> role)
                    .toList();
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUserId(), null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtExpiredException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
