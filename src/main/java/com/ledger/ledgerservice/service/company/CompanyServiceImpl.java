package com.ledger.ledgerservice.service.company;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.company.CreateCompanyRequest;
import com.ledger.ledgerservice.model.dto.request.company.SearchCompany;
import com.ledger.ledgerservice.model.dto.response.company.CompanyResponse;
import com.ledger.ledgerservice.model.dto.response.company.CompanyTreeResponse;
import com.ledger.ledgerservice.model.entity.Company;
import com.ledger.ledgerservice.model.enums.CompanyStatus;
import com.ledger.ledgerservice.model.enums.CompanyType;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.UnitLevel;
import com.ledger.ledgerservice.model.mapper.CompanyMapper;
import com.ledger.ledgerservice.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        String companyName = trimToNull(request.getName());
        String shortName = trimToNull(request.getShortName());
        String parentId = trimToNull(request.getParentId());

        UnitLevel unitLevel = resolveUnitLevel(request.getUnitLevel());
        CompanyType companyType = resolveCompanyType(request.getCompanyType());
        String companyCode = normalizeCode(companyName);

        if (companyRepository.existsByCode(companyCode)) {
            throw new BusinessException(MessageCode.COMPANY_CODE_EXISTS, HttpStatus.CONFLICT);
        }

        Company parentCompany = resolveParentCompany(parentId);
        if (parentCompany == null) {
            validateRootCompany(unitLevel);
        } else {
            validateParentChild(parentCompany, unitLevel);
        }

        Integer nextSortLevel = companyRepository.findMaxSortLevelByParentId(parentId).orElse(0) + 1;

        Company company = new Company();
        company.setName(companyName);
        company.setCode(companyCode);
        company.setShortName(shortName);
        company.setParentId(parentCompany != null ? parentCompany.getId() : null);
        company.setCompanyType(companyType);
        company.setUnitLevel(unitLevel);
        company.setSortLevel(nextSortLevel);
        company.setStatus(CompanyStatus.ACTIVE);

        Company savedCompany = companyRepository.save(company);
        return companyMapper.mapToCompany(savedCompany);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getById(String companyId) {
        Company company = getCompanyOrThrow(companyId);
        return companyMapper.mapToCompany(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyTreeResponse> search(SearchCompany searchCompany) {
        String keyword = searchCompany != null ? trimToNull(searchCompany.getKeyword()) : null;
        String companyTypeFilter = searchCompany != null ? trimToNull(searchCompany.getCompanyType()) : null;
        List<Company> companies = companyRepository.findAllByOrderBySortLevelAscCreatedAtAsc();
        return buildCompanyTree(companies, keyword, companyTypeFilter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyTreeResponse> getCompanyTree() {
        return buildCompanyTree(companyRepository.findAllByOrderBySortLevelAscCreatedAtAsc(), null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompanyResponse updateCompany(String companyId, CreateCompanyRequest request) {
        Company company = getCompanyOrThrow(companyId);

        String companyName = trimToNull(request.getName());
        String shortName = trimToNull(request.getShortName());
        String parentId = trimToNull(request.getParentId());
        UnitLevel unitLevel = resolveUnitLevel(request.getUnitLevel());
        CompanyType companyType = resolveCompanyType(request.getCompanyType());

        String newCode = normalizeCode(companyName);
        if (!newCode.equals(company.getCode()) && companyRepository.existsByCode(newCode)) {
            throw new BusinessException(MessageCode.COMPANY_CODE_EXISTS, HttpStatus.CONFLICT);
        }

        Company parentCompany = resolveParentCompany(parentId);
        if (parentCompany == null) {
            validateRootCompanyForUpdate(company, unitLevel);
        } else {
            validateParentChild(parentCompany, unitLevel);
            if (companyId.equals(parentCompany.getId())) {
                throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
            }
            if (isDescendant(companyId, parentCompany, new HashSet<>())) {
                throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
            }
        }

        Integer newSortLevel = company.getSortLevel();
        String newParentId = company.getParentId();

        boolean parentChanged = !safeEquals(company.getParentId(), parentCompany != null ? parentCompany.getId() : null);
        if (parentChanged) {
            newParentId = parentCompany != null ? parentCompany.getId() : null;
            newSortLevel = companyRepository.findMaxSortLevelByParentId(newParentId).orElse(0) + 1;
        }

        company.setName(companyName);
        company.setCode(newCode);
        company.setShortName(shortName);
        company.setParentId(newParentId);
        company.setCompanyType(companyType);
        company.setUnitLevel(unitLevel);
        company.setSortLevel(newSortLevel);

        Company savedCompany = companyRepository.save(company);
        return companyMapper.mapToCompany(savedCompany);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompanyResponse changeStatus(String companyId, CompanyStatus status) {
        if (status == null) {
            throw new BusinessException(MessageCode.COMPANY_STATUS_INVALID, HttpStatus.BAD_REQUEST);
        }
        Company company = getCompanyOrThrow(companyId);
        company.setStatus(status);
        return companyMapper.mapToCompany(companyRepository.save(company));
    }

    private Company getCompanyOrThrow(String companyId) {
        if (!StringUtils.hasText(companyId)) {
            throw new BusinessException(MessageCode.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(MessageCode.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Company resolveParentCompany(String parentId) {
        if (!StringUtils.hasText(parentId)) {
            return null;
        }
        Company parentCompany = companyRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(MessageCode.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (parentCompany.getStatus() == CompanyStatus.INACTIVE) {
            throw new BusinessException(MessageCode.COMPANY_STATUS_INVALID, HttpStatus.BAD_REQUEST);
        }
        return parentCompany;
    }

    private UnitLevel resolveUnitLevel(String value) {
        UnitLevel unitLevel = UnitLevel.fromName(trimToNull(value));
        if (unitLevel != null) {
            return unitLevel;
        }

        if (!StringUtils.hasText(value)) {
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }

        try {
            return UnitLevel.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            log.error("Invalid unit level: {}", value);
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    private CompanyType resolveCompanyType(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }

        String normalized = value.trim();
        try {
            return CompanyType.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            for (CompanyType type : CompanyType.values()) {
                if (type.getName() != null && type.getName().equalsIgnoreCase(normalized)) {
                    return type;
                }
            }
            log.error("Invalid company type: {}", value);
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateRootCompany(UnitLevel unitLevel) {
        if (unitLevel != UnitLevel.TONG_CONG_TY) {
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }

        boolean rootExist = companyRepository.existsByParentIdNullAndUnitLevel(UnitLevel.TONG_CONG_TY);
        if (rootExist) {
            log.error("Root company already exists");
            throw new BusinessException(MessageCode.CONFLICT, HttpStatus.CONFLICT);
        }
    }

    private void validateRootCompanyForUpdate(Company currentCompany, UnitLevel unitLevel) {
        // Keep the existing root valid when only non-structural fields change.
        if (unitLevel != UnitLevel.TONG_CONG_TY) {
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }

        boolean sameRootCompany = currentCompany.getParentId() == null
                && currentCompany.getUnitLevel() == UnitLevel.TONG_CONG_TY;
        if (sameRootCompany) {
            return;
        }

        validateRootCompany(unitLevel);
    }

    private void validateParentChild(Company parentCompany, UnitLevel childUnitLevel) {
        UnitLevel parentUnitLevel = parentCompany.getUnitLevel();
        if (parentUnitLevel == null) {
            log.error("Invalid unit level for parent company: {}", parentCompany.getName());
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }

        boolean valid = switch (parentUnitLevel) {
            case TONG_CONG_TY -> childUnitLevel == UnitLevel.PHONG
                    || childUnitLevel == UnitLevel.CHI_NHANH
                    || childUnitLevel == UnitLevel.CONG_TY_ME;
            case CONG_TY_ME -> childUnitLevel == UnitLevel.CHI_NHANH
                    || childUnitLevel == UnitLevel.XI_NGHIEP
                    || childUnitLevel == UnitLevel.PHONG;
            case CHI_NHANH -> childUnitLevel == UnitLevel.PHONG;
            case XI_NGHIEP -> childUnitLevel == UnitLevel.PHONG;
            case PHONG -> false;
        };

        if (!valid) {
            log.error("Invalid parent-child unit level: parent={}, child={}", parentUnitLevel, childUnitLevel);
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    private List<CompanyTreeResponse> buildCompanyTree(List<Company> companies, String keyword, String companyTypeFilter) {
        if (companies == null || companies.isEmpty()) {
            return List.of();
        }

        // Build an in-memory lookup so we can resolve parents while assembling the tree.
        Map<String, Company> companyById = companies.stream()
                .filter(company -> StringUtils.hasText(company.getId()))
                .collect(Collectors.toMap(Company::getId, company -> company, (left, right) -> left, LinkedHashMap::new));

        // Keep matched companies and also include their ancestors so the tree stays connected.
        Set<String> includedCompanyIds = new HashSet<>();
        for (Company company : companyById.values()) {
            if (matchesFilter(company, keyword, companyTypeFilter)) {
                includeWithAncestors(company, companyById, includedCompanyIds);
            }
        }

        if (includedCompanyIds.isEmpty()) {
            return List.of();
        }

        // Convert entities to response nodes and group them by parentId in one pass.
        Map<String, List<CompanyTreeResponse>> childrenByParentId = new HashMap<>();
        List<CompanyTreeResponse> nodes = new ArrayList<>();
        for (Company company : companyById.values()) {
            if (!includedCompanyIds.contains(company.getId())) {
                continue;
            }
            CompanyTreeResponse node = toTreeResponse(company);
            nodes.add(node);
            childrenByParentId.computeIfAbsent(company.getParentId(), ignored -> new ArrayList<>()).add(node);
        }

        // Sort siblings before linking children to parents.
        for (List<CompanyTreeResponse> children : childrenByParentId.values()) {
            children.sort(treeComparator());
        }

        // Root nodes are those without a parent in the filtered result set.
        List<CompanyTreeResponse> roots = new ArrayList<>();
        for (CompanyTreeResponse node : nodes) {
            if (!StringUtils.hasText(node.getParentId()) || !includedCompanyIds.contains(node.getParentId())) {
                roots.add(node);
            }
        }

        roots.sort(treeComparator());
        // Recursively attach descendants to each root node.
        for (CompanyTreeResponse root : roots) {
            attachChildren(root, childrenByParentId);
        }

        return roots;
    }

    private void includeWithAncestors(Company company, Map<String, Company> companyById, Set<String> includedCompanyIds) {
        Company current = company;
        while (current != null && includedCompanyIds.add(current.getId())) {
            String parentId = current.getParentId();
            if (!StringUtils.hasText(parentId)) {
                break;
            }
            current = companyById.get(parentId);
        }
    }

    private void attachChildren(CompanyTreeResponse node, Map<String, List<CompanyTreeResponse>> childrenByParentId) {
        List<CompanyTreeResponse> children = childrenByParentId.get(node.getId());
        if (children == null || children.isEmpty()) {
            node.setChildren(List.of());
            return;
        }

        for (CompanyTreeResponse child : children) {
            attachChildren(child, childrenByParentId);
        }
        node.setChildren(children);
    }

    private CompanyTreeResponse toTreeResponse(Company company) {
        return CompanyTreeResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .companyType(company.getCompanyType() != null ? company.getCompanyType().name() : null)
                .parentId(company.getParentId())
                .shortName(company.getShortName())
                .unitLevel(company.getUnitLevel() != null ? company.getUnitLevel().name() : null)
                .code(company.getCode())
                .status(company.getStatus() != null ? company.getStatus().name() : null)
                .sortLevel(company.getSortLevel())
                .createdAt(company.getCreatedAt())
                .children(new ArrayList<>())
                .build();
    }

    private Comparator<CompanyTreeResponse> treeComparator() {
        return Comparator.comparing(CompanyTreeResponse::getSortLevel, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(CompanyTreeResponse::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CompanyTreeResponse::getName, Comparator.nullsLast(String::compareToIgnoreCase));
    }

    private String normalizeCode(String name) {
        if (name == null) {
            return null;
        }
        String result = name.trim();
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = result.replaceAll("\\p{M}", "");
        result = result.toUpperCase(Locale.ROOT);
        result = result.replaceAll("[^A-Z0-9]", "_");
        result = result.replaceAll("^_+|_+$", "");
        return result;
    }

    private boolean matchesFilter(Company company, String keyword, String companyTypeFilter) {
        boolean keywordMatched = !StringUtils.hasText(keyword)
                || containsIgnoreCase(company.getName(), keyword)
                || containsIgnoreCase(company.getShortName(), keyword)
                || containsIgnoreCase(company.getCode(), keyword)
                || containsIgnoreCase(company.getId(), keyword);

        boolean typeMatched = !StringUtils.hasText(companyTypeFilter)
                || matchesCompanyType(company.getCompanyType(), companyTypeFilter);

        return keywordMatched && typeMatched;
    }

    private boolean matchesCompanyType(CompanyType companyType, String filter) {
        if (companyType == null || !StringUtils.hasText(filter)) {
            return false;
        }
        String normalizedFilter = filter.trim();
        if (companyType.name().equalsIgnoreCase(normalizedFilter)) {
            return true;
        }
        return companyType.getName() != null && companyType.getName().equalsIgnoreCase(normalizedFilter);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return StringUtils.hasText(value) && StringUtils.hasText(keyword)
                && value.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private boolean isDescendant(String ancestorCompanyId, Company company, Set<String> visited) {
        if (company == null || !StringUtils.hasText(company.getParentId())) {
            return false;
        }
        // Guard against corrupted data that could create a parent cycle.
        if (!visited.add(company.getId())) {
            return false;
        }
        if (ancestorCompanyId.equals(company.getParentId())) {
            return true;
        }
        Company parent = companyRepository.findById(company.getParentId()).orElse(null);
        return isDescendant(ancestorCompanyId, parent, visited);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean safeEquals(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }
}
