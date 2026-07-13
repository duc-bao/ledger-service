package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.Company;
import com.ledger.ledgerservice.model.enums.UnitLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
    boolean existsByCode(String code);

    boolean existsByParentIdNullAndUnitLevel(UnitLevel unitLevel);

    @Query("SELECT COALESCE(MAX(c.sortLevel), 0) FROM Company c WHERE (:parentId IS NULL AND c.parentId IS NULL) OR c.parentId = :parentId")
    Optional<Integer> findMaxSortLevelByParentId(@Param("parentId") String parentId);

    List<Company> findAllByOrderBySortLevelAscCreatedAtAsc();

    List<Company> findAllByParentIdIsNullOrderBySortLevelAscCreatedAtAsc();

    List<Company> findAllByParentIdOrderBySortLevelAscCreatedAtAsc(String parentId);
}
