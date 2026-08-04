package com.talkkia.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SubCategory {

    @Id
    @GeneratedValue
    Long id;
    String subCategoryName;
    String subCategoryPicture;

}
