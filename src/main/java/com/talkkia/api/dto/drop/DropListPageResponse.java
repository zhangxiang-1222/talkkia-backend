package com.talkkia.api.dto.drop;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropListPageResponse {
    private DropCityOption selectedCity;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DropCityOption> cityOptions;

    private List<DropListItem> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Long totalPages;
    private Boolean hasNext;
}
