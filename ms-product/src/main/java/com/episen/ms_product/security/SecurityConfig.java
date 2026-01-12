package com.episen.ms_product.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthentificationFilter jwtAuthentificationFilter;

    public SecurityConfig(JwtAuthentificationFilter jwtAuthentificationFilter) {
        this.jwtAuthentificationFilter = jwtAuthentificationFilter;
    }

    /**
     * Configure la chaîne de filtres de sécurité.
     * @param httpSecurity L'objet HttpSecurity à configurer.
     * @return La chaîne de filtres de sécurité configurée.
     * @throws Exception En cas d'erreur de configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthentificationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
