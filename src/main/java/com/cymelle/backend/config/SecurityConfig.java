package com.cymelle.backend.config;

import com.cymelle.backend.security.CustomAccessDeniedHandler;
import com.cymelle.backend.security.JwtAuthenticationEntryPoint;
import com.cymelle.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.cymelle.backend.model.Role.ADMIN;
import static com.cymelle.backend.model.Role.CUSTOMER;
import static com.cymelle.backend.model.Role.DRIVER;
import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers("/api/v1/auth/**").permitAll()
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                                .requestMatchers(GET, "/api/v1/products/**").permitAll()
                                .requestMatchers(POST, "/api/v1/products/**").hasRole(ADMIN.name())
                                .requestMatchers(PUT, "/api/v1/products/**").hasRole(ADMIN.name())
                                .requestMatchers(DELETE, "/api/v1/products/**").hasRole(ADMIN.name())
                                .requestMatchers(PATCH, "/api/v1/orders/*/status").hasRole(ADMIN.name())
                                .requestMatchers(PATCH, "/api/v1/rides/*/status").hasAnyRole(ADMIN.name(), DRIVER.name())
                                .requestMatchers("/api/v1/orders/**").authenticated()
                                .requestMatchers("/api/v1/rides/**").authenticated()
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
