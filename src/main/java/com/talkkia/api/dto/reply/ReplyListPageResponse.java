package com.talkkia.api.dto.reply;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyListPageResponse {
    private Long dropId;
    private List<ReplyListItem> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Long totalPages;
    private Boolean hasNext;
}
