package com.ledger.ledgerservice.service.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.ledgerservice.model.constant.CommonConstant;
import com.ledger.ledgerservice.model.dto.request.MenuSeedItem;
import com.ledger.ledgerservice.model.dto.request.MenuSeedWrapper;
import com.ledger.ledgerservice.model.entity.Menu;
import com.ledger.ledgerservice.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuSeedService implements ApplicationRunner {
    private static final String SEED_FILE = "seed/menus.json";

    private final MenuRepository menuRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (menuRepository.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource(SEED_FILE);
        if (!resource.exists()) {
            log.warn("Seed file not found: {}", SEED_FILE);
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            MenuSeedWrapper wrapper = objectMapper.readValue(inputStream, MenuSeedWrapper.class);
            if (wrapper == null || wrapper.getMenus() == null || wrapper.getMenus().isEmpty()) {
                log.warn("No menu data in seed file {}", SEED_FILE);
                return;
            }

            Map<String, MenuSeedItem> byCode = wrapper.getMenus().stream()
                    .filter(m -> StringUtils.hasText(m.getCode()))
                    .collect(Collectors.toMap(
                            m -> m.getCode().toUpperCase(),
                            Function.identity(),
                            (left, right) -> right,
                            LinkedHashMap::new
                    ));

            Map<String, Menu> seededMenus = new LinkedHashMap<>();
            for (MenuSeedItem item : byCode.values()) {
                Menu menu = Menu.builder()
                        .code(item.getCode().toUpperCase())
                        .path(item.getPath())
                        .icon(item.getIcon())
                        .actions(item.getActions() != null ? item.getActions() : new ArrayList<>())
                        .offset(item.getOffset() != null ? item.getOffset() : 0)
                        .parentId(null)
                        .ancestors(new ArrayList<>())
                        .createdBy(CommonConstant.USERNAME_SYSTEM)
                        .updatedBy(CommonConstant.USERNAME_SYSTEM)
                        .build();
                seededMenus.put(menu.getCode(), menuRepository.save(menu));
            }

            for (MenuSeedItem item : byCode.values()) {
                if (!StringUtils.hasText(item.getParentCode())) {
                    continue;
                }
                Menu child = seededMenus.get(item.getCode().toUpperCase());
                Menu parent = seededMenus.get(item.getParentCode().toUpperCase());
                if (child == null || parent == null) {
                    continue;
                }
                child.setParentId(parent.getId());
                List<String> ancestors = new ArrayList<>(parent.getAncestors() != null ? parent.getAncestors() : List.of());
                ancestors.add(parent.getId());
                child.setAncestors(ancestors);
            }

            menuRepository.saveAll(seededMenus.values());
            log.info("Seeded {} menus from {}", seededMenus.size(), SEED_FILE);
        }
    }
}
