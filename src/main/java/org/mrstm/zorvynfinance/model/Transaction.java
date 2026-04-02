package org.mrstm.zorvynfinance.model;


import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Document(collection = "transactions")
@CompoundIndexes({
        @CompoundIndex(name = "user_date_deleted_idx", def = "{'userId': 1, 'date': -1, 'deleted': 1}"),
        @CompoundIndex(name = "user_type_deleted_idx", def = "{'userId': 1, 'type': 1, 'deleted': 1}")
})
@AllArgsConstructor
@NoArgsConstructor
public class Transaction extends BaseModel {
    private Type type;
    private Category category;
    private LocalDate date;
    private String description;

    private long amount;

    private String userId;

    private boolean deleted;
    private Instant deletedAt;
}
