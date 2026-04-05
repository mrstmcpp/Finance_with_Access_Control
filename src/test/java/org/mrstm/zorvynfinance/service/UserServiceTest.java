package org.mrstm.zorvynfinance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mrstm.zorvynfinance.dto.User.UserManagementResponse;
import org.mrstm.zorvynfinance.exception.InvalidOperationException;
import org.mrstm.zorvynfinance.model.User;
import org.mrstm.zorvynfinance.repository.UserRepository;
import org.mrstm.zorvynfinance.util.Role;
import org.mrstm.zorvynfinance.util.Status;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void updateUserStatus_updatesStatusWhenActorIsActiveAdmin() {
        String adminId = "admin-1";
        String targetUserId = "user-1";

        User admin = buildUser(adminId, Role.ADMIN, Status.ACTIVE);
        User target = buildUser(targetUserId, Role.VIEWER, Status.ACTIVE);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);

        UserManagementResponse response = userService.updateUserStatus(adminId, targetUserId, Status.INACTIVE);

        assertEquals(Status.INACTIVE, response.getStatus());
        verify(userRepository).save(target);
    }

    @Test
    void promoteUser_setsPromotionAuditFields() {
        String adminId = "admin-1";
        String targetUserId = "user-1";

        User admin = buildUser(adminId, Role.ADMIN, Status.ACTIVE);
        User target = buildUser(targetUserId, Role.VIEWER, Status.ACTIVE);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);

        UserManagementResponse response = userService.promoteUser(adminId, targetUserId, Role.ANALYST);

        assertEquals(Role.ANALYST, response.getRole());
        assertEquals(adminId, response.getPromotedByUserId());
        assertNotNull(response.getPromotionDate());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(adminId, saved.getPromotedByUserId());
        assertNotNull(saved.getPromotionDate());
    }

    @Test
    void promoteUser_rejectsViewerAsTargetRole() {
        String adminId = "admin-1";
        String targetUserId = "user-1";

        User admin = buildUser(adminId, Role.ADMIN, Status.ACTIVE);
        User target = buildUser(targetUserId, Role.VIEWER, Status.ACTIVE);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(target));

        assertThrows(InvalidOperationException.class,
                () -> userService.promoteUser(adminId, targetUserId, Role.VIEWER));
    }

    @Test
    void promoteUser_rejectsDemotion() {
        String adminId = "admin-1";
        String targetUserId = "user-1";

        User admin = buildUser(adminId, Role.ADMIN, Status.ACTIVE);
        User target = buildUser(targetUserId, Role.ADMIN, Status.ACTIVE);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(target));

        assertThrows(InvalidOperationException.class,
                () -> userService.promoteUser(adminId, targetUserId, Role.ANALYST));
    }

    private User buildUser(String id, Role role, Status status) {
        User user = User.builder()
                .username(id + "-username")
                .password("encoded-password")
                .role(role)
                .status(status)
                .build();
        user.setId(id);
        return user;
    }
}


