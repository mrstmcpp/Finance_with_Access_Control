package org.mrstm.zorvynfinance.config;


import org.mrstm.zorvynfinance.service.JwtService;
import org.mrstm.zorvynfinance.util.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService) {
        return new JwtAuthFilter(jwtService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter, RateLimitFilter rateLimitFilter) throws Exception {
        http
                .csrf(c -> c.disable()) // Disable CSRF for stateless JWT
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // Allow registration/login

                        // Viewer can only view dashboard, analyst/admin can also view dashboard.
                        .requestMatchers(HttpMethod.GET, "/dashboard", "/dashboard/**")
                            .hasAnyAuthority(Role.VIEWER.toString(), Role.ANALYST.toString(), Role.ADMIN.toString())

                        // Read transaction records is for analyst/admin.
                        .requestMatchers(HttpMethod.GET, "/transactions", "/transactions/**")
                            .hasAnyAuthority(Role.ANALYST.toString(), Role.ADMIN.toString())

                        // Mutating transactions requires admin.
                        .requestMatchers(HttpMethod.POST, "/transactions", "/transactions/**")
                            .hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PUT, "/transactions", "/transactions/**")
                            .hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PATCH, "/transactions", "/transactions/**")
                            .hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.DELETE, "/transactions", "/transactions/**")
                            .hasAuthority(Role.ADMIN.toString())

                        // Keep legacy finance routes admin/analyst gated.
                        .requestMatchers(HttpMethod.GET, "/finance", "/finance/**")
                            .hasAnyAuthority(Role.ANALYST.toString(), Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.POST, "/finance", "/finance/**")
                            .hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PUT, "/finance", "/finance/**")
                            .hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PATCH, "/finance", "/finance/**")
                            .hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.DELETE, "/finance", "/finance/**")
                            .hasAuthority(Role.ADMIN.toString())

                        .requestMatchers("/users/**")
                            .hasAuthority(Role.ADMIN.toString())
                        .anyRequest().authenticated() // All other requests need authentication
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"message\":\"Authentication is required\",\"status\":401,\"timestamp\":\"" + LocalDateTime.now() + "\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"message\":\"You do not have permission to perform this action\",\"status\":403,\"timestamp\":\"" + LocalDateTime.now() + "\"}");
                        })
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // No sessions

        // JWT first, then rate limiting so authenticated calls can be keyed by user id.
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(rateLimitFilter, JwtAuthFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
