package com.talkkia.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Article {

    @Id
    @GeneratedValue
    Long id;
    String articleURL;
    String title;
    String urlToImage;
    String description;
    Integer likes;
    Integer dislikes;
    Integer shared;
    Long createdTimestamp;
    @Relationship(value = "IS_OF_SUBCATEGORY")
    List<SubCategory> subCategories;
}
