package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ledger.ledgerservice.util.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "idp_permissions",
  indexes = {
    @Index(columnList = "menu_id"),
    @Index(columnList = "group_id"),
    @Index(columnList = "user_id"),
    @Index(columnList = "discriminator")
  }
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Permission extends EntityBase {
  @Column(name = "menu_id")
  private String menuId;

  @Column(name = "group_id")
  private String groupId;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "discriminator")
  private String discriminator;

  @Column(name = "actions")
  @Convert(converter = StringListConverter.class)
  @Builder.Default
  private List<String> actions = new ArrayList<>();
}
