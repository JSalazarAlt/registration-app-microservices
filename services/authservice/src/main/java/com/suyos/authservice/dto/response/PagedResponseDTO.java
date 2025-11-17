package com.suyos.authservice.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic wrapper for paginated API responses.
 * 
 * Provides a standardized structure for returning paginated data
 * from REST endpoints. It includes both the data content and pagination
 * metadata that clients need to implement pagination controls.
 * 
 * @param <T> Type of objects contained in the paginated response
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponseDTO<T> {
    
    /**
     * Actual data content for the current page.
     */
    private List<T> content;
    
    /**
     * Zero-based index of the current page.
     */
    private int currentPage;
    
    /**
     * Total number of pages available.
     */
    private int totalPages;
    
    /**
     * Total number of elements across all pages.
     */
    private long totalElements;
    
    /**
     * Number of elements per page (page size).
     */
    private int size;
    
    /**
     * Indicates if this is the first page.
     */
    private boolean first;
    
    /**
     * Indicates if this is the last page.
     */
    private boolean last;
    
}