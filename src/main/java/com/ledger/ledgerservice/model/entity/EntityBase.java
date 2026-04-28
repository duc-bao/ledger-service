package com.ledger.ledgerservice.model.entity;

import com.ledger.ledgerservice.model.constant.CommonConstant;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@SuperBuilder
public class EntityBase {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private String id;

    @Order(3)
    @Column(name = "created_at", updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Order(4)
    @Column(name = "created_by", updatable = false)
    @Builder.Default
    private String createdBy = CommonConstant.USERNAME_SYSTEM;

    @Order(5)
    @Column(name = "updated_at")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Order(6)
    @Column(name = "updated_by")
    @Builder.Default
    private String updatedBy = CommonConstant.USERNAME_SYSTEM;

    @Column(name = "description", length = 500)
    private String description;
}
