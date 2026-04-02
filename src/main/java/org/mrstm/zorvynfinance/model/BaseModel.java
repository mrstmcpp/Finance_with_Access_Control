package org.mrstm.zorvynfinance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseModel {
    @Id
    private String id;

    @LastModifiedDate
    private Instant  updatedAt;

    @CreatedDate
    private Instant createdAt;
}
