package com.suyos.common.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic wrapper for paginated API responses.
 * 
 * <p>Contains paginated data and metadata used to support pagination controls
 * in client applications.</p>
 * 
 * @param <T> Type of objects contained in the paginated response
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponseDTO<T> {
    
    /** Actual data content for current page */
    private List<T> content;
    
    /** Zero-based index of current page */
    private int currentPage;
    
    /** Total number of pages available */
    private int totalPages;
    
    /** Total number of elements across all pages */
    private long totalElements;
    
    /** Number of elements per page (page size) */
    private int size;
    
    /** Flag indicating if current page is first page */
    private boolean first;
    
    /** Flag indicating if current page is last page */
    private boolean last;
    
}
