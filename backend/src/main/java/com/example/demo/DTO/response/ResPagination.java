package com.example.demo.DTO.response;

import lombok.*;

@Getter
@Setter
@Builder
public class ResPagination {
    private MetaDTO meta;
    private Object data;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MetaDTO {
        private int page;
        private int pageSize;
        private int pages;
        private long total;
    }
}
