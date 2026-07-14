package com.ledger.ledgerservice.repository.specification;

import com.ledger.ledgerservice.model.dto.request.PermissionApiSearchRequest;
import com.ledger.ledgerservice.model.entity.PermissionApi;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class PermissionApiSpecification {
    private PermissionApiSpecification() {
    }

    public static Specification<PermissionApi> bySearchRequest(PermissionApiSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            if (request == null) {
                return predicate;
            }
            if (StringUtils.hasText(request.getPermissionId())) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("permissionId"), request.getPermissionId().trim()));
            }
            if (StringUtils.hasText(request.getServiceCode())) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(criteriaBuilder.upper(root.get("serviceCode")), request.getServiceCode().trim().toUpperCase()));
            }
            if (StringUtils.hasText(request.getHttpMethod())) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(criteriaBuilder.upper(root.get("httpMethod")), request.getHttpMethod().trim().toUpperCase()));
            }
            if (StringUtils.hasText(request.getUriPattern())) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("uriPattern")), "%" + request.getUriPattern().trim().toLowerCase() + "%"));
            }
            if (request.getMatchType() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("matchType"), request.getMatchType()));
            }
            if (request.getStatus() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }
            return predicate;
        };
    }
}