package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, String> {
    Optional<Menu> findByCode(String code);
    List<Menu> findAllByOrderByOffsetAscCodeAsc();

    @Query("SELECT m FROM Menu m WHERE UPPER(m.code) IN :codes ORDER BY m.offset ASC, m.code ASC")
    List<Menu> findByCodeInOrderByOffset(@Param("codes") Collection<String> codes);
}
