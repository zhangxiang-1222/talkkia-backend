package com.talkkia.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    SearchType searchType;
    String keyword;
    boolean includeUserPosts = true;

}

