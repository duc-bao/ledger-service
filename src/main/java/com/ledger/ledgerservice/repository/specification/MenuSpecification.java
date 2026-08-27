package com.ledger.ledgerservice.repository.specification;

import com.ledger.ledgerservice.model.dto.request.MenuSearchRequest;
import com.ledger.ledgerservice.model.entity.Menu;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class MenuSpecification {
    private MenuSpecification() {
    }

    public static Specification<Menu> bySearchRequest(MenuSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            if (request == null) {
                return predicate;
            }

            if (StringUtils.hasText(request.getKeyword())) {
                String keyword = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("path")), keyword)
                ));
            }
            if (StringUtils.hasText(request.getParentId())) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("parentId"), request.getParentId().trim()));
            }
            if (request.getMenuType() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("menuType"), request.getMenuType()));
            }
            if (request.getVisible() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("visible"), request.getVisible()));
            }
            if (request.getStatus() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }
            return predicate;
        };
    }
}
