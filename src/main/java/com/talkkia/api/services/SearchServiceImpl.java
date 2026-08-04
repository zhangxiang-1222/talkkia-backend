package com.talkkia.api.services;

import com.talkkia.api.entity.User;
import com.talkkia.api.model.SearchRequest;
import com.talkkia.api.model.SearchResponse;
import com.talkkia.api.repositories.ArticleRepository;
import com.talkkia.api.repositories.BusinessPostRepository;
import com.talkkia.api.repositories.PostRepository;
import com.talkkia.api.repositories.UserRepository;
import com.talkkia.api.view.ArticleView;
import com.talkkia.api.view.BusinessPostView;
import com.talkkia.api.view.PostView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService{

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    BusinessPostRepository businessPostRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public CompletableFuture<SearchResponse> getSearchResults(SearchRequest request,Long userId,Pageable pageable) {
        CompletableFuture<SearchResponse> completableFuture = new CompletableFuture<>();
        SearchResponse response = new SearchResponse();
        CompletableFuture<Optional<Page<BusinessPostView>>> businessPosts;
        CompletableFuture<Optional<Page<ArticleView>>> articles;
        CompletableFuture<Optional<Page<PostView>>> posts ;
        try {
            switch (request.getSearchType()) {
                case ALL:
                    businessPosts = getBusinessPosts(request.getKeyword(),pageable);
                    articles = getNewsArticles(request.getKeyword(),pageable);
                    posts = getPosts(request.getKeyword(), userId,pageable,request.isIncludeUserPosts());
                   CompletableFuture future =  CompletableFuture.allOf(businessPosts, articles ,posts);
                   future.thenApplyAsync((value)->{
                        Optional<Page<BusinessPostView>> businessPostViews = businessPosts.join();
                        if(businessPostViews != null) {
                            response.setBusinessPosts(businessPostViews.get());
                        }
                        Optional<Page<ArticleView>> articleViews = articles.join();
                        if(articleViews != null) {
                            response.setNewsArticles(articleViews.get());
                        }
                        Optional<Page<PostView>> postViews = posts.join();
                        if(postViews != null) {
                            response.setPosts(postViews.get());
                        }
                        return completableFuture.complete(response);
                   });
                    break;
                case BUSINESS_POST:
                    businessPosts = getBusinessPosts(request.getKeyword(),pageable);
                    businessPosts.whenCompleteAsync((result,error)-> {
                                if (error == null) {

                                    if(result != null) {
                                        response.setBusinessPosts(result.get());
                                    }
                                    completableFuture.complete(response);
                                }else{
                                    completableFuture.completeExceptionally(error);
                                }
                            }
                    );
                    break;
                case NEWS_ARTICLE:
                    articles = getNewsArticles(request.getKeyword(),pageable);
                    articles.whenCompleteAsync((result,error)-> {
                                if (error == null) {
                                    if(result != null) {
                                        response.setNewsArticles(result.get());
                                    }
                                    completableFuture.complete(response);
                                }else{

                                   completableFuture.completeExceptionally(error);
                                }
                            }
                    );
                    break;
                case POST:
                    posts = getPosts(request.getKeyword(), userId,pageable,request.isIncludeUserPosts());
                    posts.whenCompleteAsync((result,error)-> {
                        if (error == null) {
                            if(result != null) {
                                response.setPosts(result.get());
                            }
                            completableFuture.complete(response);
                        }else{
                            completableFuture.completeExceptionally(error);
                        }
                    }
                    );

                    break;


            }
        }catch (Exception e){
            completableFuture.completeExceptionally(e);
        }

        return completableFuture;
    }

    @Override
    public CompletableFuture<Optional<Page<BusinessPostView>>> getBusinessPosts(String keyword, Pageable pageable) {
       return CompletableFuture.supplyAsync(()-> businessPostRepository.findByPostTitleContainsIgnoreCase(keyword,pageable));
    }


    /*
     @Override
    public CompletableFuture<Optional<Page<ArticleView>>> getNewsArticles(String keyword, Pageable pageable, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if(user.getSubCategories().size()==0){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not have subcategories subscribed");
        }
        return CompletableFuture.supplyAsync(()->
                articleRepository.findArticlesByTitleContainsIgnoreCaseAndSubCategoriesIdIn(keyword,pageable,user.getSubCategories().stream().map(SubCategory::getId).collect(Collectors.toList())));
    }

     */

    @Override
    public CompletableFuture<Optional<Page<ArticleView>>> getNewsArticles(String keyword, Pageable pageable) {
        return CompletableFuture.supplyAsync(()->
                articleRepository.findArticlesByTitleContainsIgnoreCase(keyword,pageable));
    }
    @Override
    public CompletableFuture<Optional<Page<PostView>>> getPosts(String keyword, Long userId, Pageable pageable,boolean includeUserPosts) {
        User user = userRepository.findById(userId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<Long> userIds = user.getContacts().stream().map(creator -> creator.getUser().getId()).collect(Collectors.toList());
//        List<User> contacts = user.getContacts();
//        List<Long> userIds = contacts.stream().map(User::getId).collect(Collectors.toList()) ;
        if(includeUserPosts) {
            userIds.add(user.getId());
        }
        return CompletableFuture.supplyAsync(()->postRepository.findAllByPostTextContainsIgnoreCaseAndPostCreatorIdIn(keyword,userIds,pageable));
    }


}
