package com.ledger.ledgerservice.service.seed;

import com.ledger.ledgerservice.config.properties.AppSettingProperty;
import com.ledger.ledgerservice.model.constant.CommonConstant;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserSeedService implements ApplicationRunner {
    private final UserRepository userRepository;
    private final AppSettingProperty appSettingProperty;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String username = appSettingProperty.getSuperUser();
        String password = appSettingProperty.getPassword();
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            log.warn("Skip init admin user because app-setting.superUser/password is empty");
            return;
        }

        if (userRepository.findByUsername(username).isPresent()) {
            log.info("Admin user '{}' already exists, skip init", username);
            return;
        }

        User admin = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .fullName("Administrator")
                .userType("SYSTEM")
                .email("truongducbaosavyint@gmail.com")
                .requireChange(false)
                .createdBy(CommonConstant.USERNAME_SYSTEM)
                .updatedBy(CommonConstant.USERNAME_SYSTEM)
                .build();
        userRepository.save(admin);
        log.info("Initialized admin user '{}' from app-setting", username);
    }
}
