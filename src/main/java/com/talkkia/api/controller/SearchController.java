package com.talkkia.api.controller;

import com.talkkia.api.model.SearchRequest;
import com.talkkia.api.services.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

//this code is written by another developer and need to be reviewed to match to our code convention.
@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    SearchService searchService;

    @PostMapping("")
    public DeferredResult<ResponseEntity> getSearchResults(@PageableDefault Pageable pageable, @RequestBody SearchRequest request , @RequestHeader("userId") Long userId){
        DeferredResult result = new DeferredResult();
        try {

         searchService.getSearchResults(request, userId, pageable).whenComplete(
                 (value,error)->{
                     if(error==null) {
                         result.setResult(value);
                     }else
                         result.setErrorResult(error);
                 }
         );
        }catch (Exception e) {
            result.setErrorResult(e);
        }
        return result;
    }
}
