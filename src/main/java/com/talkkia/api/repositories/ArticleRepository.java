package com.talkkia.api.repositories;

import com.talkkia.api.entity.Article;
import com.talkkia.api.view.ArticleView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.Optional;

public interface ArticleRepository  extends Neo4jRepository<Article,Long> {

    @Query(value = "MATCH (a:Article)-[:IS_OF_SUBCATEGORY]-(s:SubCategory)-[:IS_SUBSCRIBER]-(u:User) where u.id=$userId  RETURN a SKIP $skip LIMIT $limit ",
    countQuery = "MATCH (a:Article)-[:IS_OF_SUBCATEGORY]-(s:SubCategory)-[:IS_SUBSCRIBER]-(u:User) where u.id=$userId RETURN count(a)")
    Page<ArticleView> findAllBySubCategoriesAndId(Long userId,Pageable pageable);

    //User should not be restricted to his selection as per Mehran Khan. Can be modified in future.
    //Optional<Page<ArticleView>> findArticlesByTitleContainsIgnoreCaseAndSubCategoriesIdIn(String keyword, Pageable pageable, List<Long> subCategories);

    Optional<Page<ArticleView>> findArticlesByTitleContainsIgnoreCase(String keyword, Pageable pageable);
    ArticleView findArticleById(Long id);



}
