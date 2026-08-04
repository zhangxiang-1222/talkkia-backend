package com.talkkia.api.services;

import com.talkkia.api.model.SearchRequest;
import com.talkkia.api.model.SearchResponse;
import com.talkkia.api.view.ArticleView;
import com.talkkia.api.view.BusinessPostView;
import com.talkkia.api.view.PostView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SearchService{

    CompletableFuture<SearchResponse> getSearchResults(SearchRequest request,Long userId,Pageable pageable);

    CompletableFuture<Optional<Page<BusinessPostView>>> getBusinessPosts(String keyword, Pageable pageable);

    //CompletableFuture<Optional<Page<ArticleView>>> getNewsArticles(String keyword,Pageable pageable,Long userId);
    CompletableFuture<Optional<Page<ArticleView>>> getNewsArticles(String keyword, Pageable pageable);
    CompletableFuture<Optional<Page<PostView>>> getPosts(String keyword, Long userId,Pageable pageable,boolean includeUserPosts);


}
