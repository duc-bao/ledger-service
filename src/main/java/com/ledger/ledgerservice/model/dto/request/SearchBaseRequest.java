package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SearchBaseRequest {
    @Size(max = 200, message = "validation.search.keyword.maxLength")
    private String keyword;

    @Min(value = 0, message = "validation.search.page.min")
    @Max(value = 100000, message = "validation.search.page.max")
    @Builder.Default
    private Integer page = 1;

    @Min(value = 1, message = "validation.search.size.min")
    @Max(value = 200, message = "validation.search.size.max")
    @Builder.Default
    private Integer size = 10;

    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{0,49}$", message = "validation.search.sort.invalid")
    @Builder.Default
    private String sort = "createdAt";

    @Pattern(regexp = "^(?i)(ASC|DESC)$", message = "validation.search.order.invalid")
    @Builder.Default
    private String order = "DESC";

}
