package com.ledger.ledgerservice.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response payload")
public class PageResponse<T> {
    @Schema(description = "Current page items")
    private List<T> items;

    @Schema(description = "Total number of elements", example = "25")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "3")
    private int totalPages;

    @Schema(description = "Current page number (1-based)", example = "1")
    private int page;

    @Schema(description = "Current page size", example = "10")
    private int size;
}