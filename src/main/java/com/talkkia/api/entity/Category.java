package com.talkkia.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Category {

    @Id
    @GeneratedValue
    Long id;
    String categoryName;
    String categoryPicture;
    @Relationship(value = "HAS_SUBCATEGORY")
    List<SubCategory> subCategoryList = new ArrayList<>();
}
