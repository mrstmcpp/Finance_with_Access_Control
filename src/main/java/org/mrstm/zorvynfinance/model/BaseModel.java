package org.mrstm.zorvynfinance.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseModel {
    @Id
    private String id;

    @LastModifiedDate
    private Date updatedAt;

    @CreatedDate
    private Date createdAt;
}
