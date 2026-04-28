package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ledger.ledgerservice.util.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sys_menus",
  indexes = {
    @Index(columnList = "code"),
    @Index(columnList = "parent_id"),
    @Index(columnList = "ancestors"),
    @Index(columnList = "created_at")
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
}
