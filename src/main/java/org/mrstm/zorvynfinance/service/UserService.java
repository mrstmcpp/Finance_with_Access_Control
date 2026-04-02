package org.mrstm.zorvynfinance.service;

import org.mrstm.zorvynfinance.dto.User.AuthRequest;
import org.mrstm.zorvynfinance.dto.User.AuthResponse;
import org.mrstm.zorvynfinance.exception.InvalidCredentialsException;
import org.mrstm.zorvynfinance.exception.UserAlreadyExistsException;
import org.mrstm.zorvynfinance.model.User;
import org.mrstm.zorvynfinance.repository.UserRepository;
import org.mrstm.zorvynfinance.util.Role;
import org.mrstm.zorvynfinance.util.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public ResponseEntity<?> loginWithUsernameAndPassword(AuthRequest authRequest) {
            Optional<User> user = userRepository.findByUsername(authRequest.getUsername());

            if(user.isPresent() &&
                    user.get().getStatus() == Status.ACTIVE &&
                    passwordEncoder.matches(authRequest.getPassword(), user.get().getPassword())) {

                String token = jwtService.generateToken(user.get());
                return ResponseEntity.ok(new AuthResponse(token));
            }

            throw new InvalidCredentialsException("Invalid username or password");
    }

    public ResponseEntity<?> registerNewUser(AuthRequest authRequest) throws UserAlreadyExistsException {
            if(userRepository.findByUsername(authRequest.getUsername()).isPresent()){
                throw new UserAlreadyExistsException("An user already exists with this username");
            }

            User user = User.builder()
                    .username(authRequest.getUsername())
                    .password(passwordEncoder.encode(authRequest.getPassword()))
                    .role(Role.VIEWER)
                    .status(Status.ACTIVE)
                    .build();

            userRepository.save(user);

            String token = jwtService.generateToken(user);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(token));
    }

}
