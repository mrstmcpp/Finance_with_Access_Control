package org.mrstm.zorvynfinance.model;


import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Document(collection = "transactions")
@AllArgsConstructor
@NoArgsConstructor
public class Transaction extends BaseModel {
    private Type type;
    private Category category;
    private LocalDate date;
    private String description;

    private String userId;
}
