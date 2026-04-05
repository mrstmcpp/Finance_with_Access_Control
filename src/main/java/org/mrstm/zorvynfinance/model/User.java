package org.mrstm.zorvynfinance.model;

import lombok.*;
import org.mrstm.zorvynfinance.util.Role;
import org.mrstm.zorvynfinance.util.Status;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Document(collection = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseModel {
    private String username;
    private String password;
    private Role role;
    private Status status;
    private String promotedByUserId;
    private Instant promotionDate;
}
