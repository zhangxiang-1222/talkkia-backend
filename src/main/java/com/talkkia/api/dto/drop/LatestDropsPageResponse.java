package com.talkkia.api.dto.drop;

import java.util.List;

import com.talkkia.api.entity.Drop;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestDropsPageResponse {
    private List<Drop> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Long totalPages;
    private Boolean hasNext;
}
