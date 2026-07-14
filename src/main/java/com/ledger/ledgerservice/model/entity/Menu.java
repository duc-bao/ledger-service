package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ledger.ledgerservice.model.enums.MenuType;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.util.StringListConverter;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sys_menus",
  indexes = {
    @Index(columnList = "code"),
    @Index(columnList = "parent_id"),
    @Index(columnList = "ancestors"),
    @Index(columnList = "created_at"),
    @Index(columnList = "status"),
    @Index(columnList = "visible")
  }
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Menu extends EntityBase {
  @Column(name = "code", length = 50, unique = true, updatable = false)
  private String code;

  @Column(name = "name", length = 150, nullable = false)
  private String name;

  @Column(name = "path")
  private String path;

  @Column(name = "ancestors", length = 700)
  @Convert(converter = StringListConverter.class)
  @Builder.Default
  private List<String> ancestors = new ArrayList<>();

  @Column(name = "parent_id")
  private String parentId;

  @Column(name = "icon")
  private String icon;

  @Column(name = "actions", length = 700)
  @Convert(converter = StringListConverter.class)
  @Builder.Default
  private List<String> actions = new ArrayList<>();

  @Column(name = "menu_offset")
  private int offset;

  @Enumerated(EnumType.STRING)
  @Column(name = "menu_type", nullable = false, length = 20)
  @Builder.Default
  private MenuType menuType = MenuType.MENU;

  @Column(name = "component", length = 200)
  private String component;

  @Column(name = "visible", nullable = false)
  @Builder.Default
  private Boolean visible = Boolean.TRUE;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private RecordStatus status = RecordStatus.ACTIVE;

  @Column(name = "external_url", length = 500)
  private String externalUrl;
}

