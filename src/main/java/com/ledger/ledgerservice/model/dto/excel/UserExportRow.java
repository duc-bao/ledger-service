package com.ledger.ledgerservice.model.dto.excel;

import com.ledger.ledgerservice.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExportRow {
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String userType;
    private UserStatus status;
    private LocalDateTime createdAt;
}
