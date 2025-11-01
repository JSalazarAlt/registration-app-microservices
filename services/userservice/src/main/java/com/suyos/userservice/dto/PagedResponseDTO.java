package com.suyos.userservice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic wrapper for paginated API responses.
 *
 * <p>Provides standardized structure for returning paginated data from
 * REST endpoints. Includes data content and pagination metadata for
 * client pagination controls.</p>
 *
 * @param <T> Type of objects contained in the paginated response
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponseDTO<T> {
    
    /** Actual data content for the current page */
    private List<T> content;
    
    /** Zero-based index of the current page */
    private int currentPage;
    
    /** Total number of pages available */
    private int totalPages;
    
    /** Total number of elements across all pages */
    private long totalElements;
    
    /** Number of elements per page */
    private int size;
    
    /** Flag indicating if this is the first page */
    private boolean first;
    
    /** Flag indicating if this is the last page */
    private boolean last;
    
}
