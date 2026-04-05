package org.mrstm.zorvynfinance.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.mrstm.zorvynfinance.dto.User.PromoteUserRoleRequest;
import org.mrstm.zorvynfinance.dto.User.UpdateUserStatusRequest;
import org.mrstm.zorvynfinance.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Validated
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<?> updateUserStatus(
            Principal principal,
            @PathVariable @NotBlank(message = "userId is required") String userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserStatus(principal.getName(), userId, request.getStatus()));
    }

    @PatchMapping("/{userId}/promote")
    public ResponseEntity<?> promoteUser(
            Principal principal,
            @PathVariable @NotBlank(message = "userId is required") String userId,
            @Valid @RequestBody PromoteUserRoleRequest request
    ) {
        return ResponseEntity.ok(userService.promoteUser(principal.getName(), userId, request.getRole()));
    }
}


