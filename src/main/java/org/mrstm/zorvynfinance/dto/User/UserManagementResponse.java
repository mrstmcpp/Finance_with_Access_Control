package org.mrstm.zorvynfinance.dto.User;

import lombok.Builder;
import lombok.Data;
import org.mrstm.zorvynfinance.util.Role;
import org.mrstm.zorvynfinance.util.Status;

import java.time.Instant;

@Data
@Builder
public class UserManagementResponse {
    private String id;
    private String username;
    private Role role;
    private Status status;
    private String promotedByUserId;
    private Instant promotionDate;
}

