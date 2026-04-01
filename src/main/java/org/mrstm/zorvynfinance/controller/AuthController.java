package org.mrstm.zorvynfinance.controller;

import jakarta.validation.Valid;
import org.mrstm.zorvynfinance.dto.User.AuthRequest;
import org.mrstm.zorvynfinance.exception.InvalidCredentialsException;
import org.mrstm.zorvynfinance.exception.UserAlreadyExistsException;
import org.mrstm.zorvynfinance.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) throws InvalidCredentialsException {
        return userService.loginWithUsernameAndPassword(authRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest authRequest) throws UserAlreadyExistsException {
        return userService.registerNewUser(authRequest);
    }
}
