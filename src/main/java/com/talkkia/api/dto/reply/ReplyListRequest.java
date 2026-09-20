package com.talkkia.api.dto.reply;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyListRequest {
    private Long dropId;
    private Integer page;
    private Integer size;
}
