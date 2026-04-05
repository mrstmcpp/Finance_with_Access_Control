package org.mrstm.zorvynfinance.dto.User;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.mrstm.zorvynfinance.util.Status;

@Data
public class UpdateUserStatusRequest {
    @NotNull(message = "status is required")
    private Status status;
}

