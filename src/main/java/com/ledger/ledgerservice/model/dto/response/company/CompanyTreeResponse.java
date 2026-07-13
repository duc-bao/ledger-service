package com.ledger.ledgerservice.model.dto.response.company;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyTreeResponse {
    private String id;
    private String name;
    private String companyType;
    private String parentId;
    private String shortName;
    private String unitLevel;
    private String code;
    private String status;
    private Integer sortLevel;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Builder.Default
    private List<CompanyTreeResponse> children = new ArrayList<>();
}
