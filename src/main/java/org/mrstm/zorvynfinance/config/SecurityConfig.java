package org.mrstm.zorvynfinance.config;


import org.mrstm.zorvynfinance.service.JwtService;
import org.mrstm.zorvynfinance.service.UserService;
import org.mrstm.zorvynfinance.util.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService, UserService userService) {
        return new JwtAuthFilter(jwtService, userService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(c -> c.disable()) // Disable CSRF for stateless JWT
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // Allow registration/login
                        .requestMatchers(HttpMethod.GET, "/dashboard", "/dashboard/**")
                            .hasAnyAuthority(Role.VIEWER.toString(), Role.ANALYST.toString(), Role.ADMIN.toString())
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
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // No sessions

        // Add our JWT filter before Spring's default authentication filter
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
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
