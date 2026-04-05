package org.mrstm.zorvynfinance.dto.User;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.mrstm.zorvynfinance.util.Role;

@Data
public class PromoteUserRoleRequest {
    @NotNull(message = "role is required")
    private Role role;
}

