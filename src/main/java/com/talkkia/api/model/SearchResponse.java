package com.talkkia.api.model;

import com.talkkia.api.view.ArticleView;
import com.talkkia.api.view.BusinessPostView;
import com.talkkia.api.view.PostView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {
    Page<BusinessPostView> businessPosts;
    Page<ArticleView> newsArticles;
    Page<PostView>  posts;
}
