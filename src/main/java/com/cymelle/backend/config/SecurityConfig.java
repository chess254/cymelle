package com.cymelle.backend.config;

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
import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers("/api/v1/auth/**").permitAll()
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                                
                                // Product management: ADMIN only for modifications, Everyone (or Authenticated?) for viewing?
                                // "Implement functionality to add, update, delete, and view products."
                                // "Ensure that ... only admins can manage products."
                                // Usually viewing is open or authorized. I'll make viewing products open or authenticated, 
                                // managing products ADMIN only.
                                .requestMatchers(GET, "/api/v1/products/**").authenticated() 
                                .requestMatchers(POST, "/api/v1/products/**").hasAuthority(ADMIN.name())
                                .requestMatchers(PUT, "/api/v1/products/**").hasAuthority(ADMIN.name())
                                .requestMatchers(DELETE, "/api/v1/products/**").hasAuthority(ADMIN.name())

                                // Order/Ride: "only authenticated users can place orders/rides"
                                .requestMatchers("/api/v1/orders/**").authenticated()
                                .requestMatchers("/api/v1/rides/**").authenticated()

                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
