package com.ledger.ledgerservice.repository.specification;

import com.ledger.ledgerservice.model.dto.request.PermissionSearchRequest;
import com.ledger.ledgerservice.model.entity.Permission;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class PermissionSpecification {
    private PermissionSpecification() {
    }

    public static Specification<Permission> bySearchRequest(PermissionSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            if (request == null) {
                return predicate;
            }

            if (StringUtils.hasText(request.getKeyword())) {
                String keyword = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keyword)
                ));
            }
            if (StringUtils.hasText(request.getModuleCode())) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(criteriaBuilder.upper(root.get("moduleCode")), request.getModuleCode().trim().toUpperCase()));
            }
            if (StringUtils.hasText(request.getActionCode())) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(criteriaBuilder.upper(root.get("actionCode")), request.getActionCode().trim().toUpperCase()));
            }
            if (StringUtils.hasText(request.getResourceType())) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(criteriaBuilder.upper(root.get("resourceType")), request.getResourceType().trim().toUpperCase()));
            }
            if (request.getStatus() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }
            return predicate;
        };
    }
}