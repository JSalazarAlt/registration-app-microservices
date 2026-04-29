package com.suyos.common.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PagedResponse<T> {
    
    /** Actual data content for current page */
    private List<T> content;
    
    private int currentPage;
    
    private int totalPages;
    
    private long totalElements;
    
    private int size;
    
    private boolean first;
    
    private boolean last;
    
}