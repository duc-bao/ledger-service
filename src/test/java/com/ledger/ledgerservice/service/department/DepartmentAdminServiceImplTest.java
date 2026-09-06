package com.ledger.ledgerservice.service.department;

import com.ledger.ledgerservice.model.dto.request.DepartmentSearchRequest;
import com.ledger.ledgerservice.model.dto.response.DepartmentResponse;
import com.ledger.ledgerservice.model.entity.DepartmentEntity;
import com.ledger.ledgerservice.repository.DepartmentRepository;
import com.ledger.ledgerservice.model.dto.response.DepartmentTreeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentAdminServiceImplTest {
    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentAdminServiceImpl service;

    @Test
    void getDepartmentReturnsDetail() {
        DepartmentEntity department = department();
        when(departmentRepository.findById("dep-1")).thenReturn(Optional.of(department));

        DepartmentResponse response = service.getDepartment("dep-1");

        assertEquals("dep-1", response.getId());
        assertEquals("FIN", response.getCode());
    }

    @Test
    void searchDepartmentsFiltersByKeywordAndStatus() {
        DepartmentSearchRequest request = new DepartmentSearchRequest();
        request.setKeyword("fin");
        request.setStatus("ACTIVE");
        request.setIsActive(true);
        request.setPage(1);
        request.setSize(20);
        when(departmentRepository.searchDepartments("fin", "ACTIVE", true, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(department()), PageRequest.of(0, 20), 1));

        Page<DepartmentResponse> page = service.searchDepartments(request);

        assertEquals(1, page.getTotalElements());
        assertEquals("Finance", page.getContent().getFirst().getName());
    }

    @Test
    void getDepartmentTreeReturnsHierarchicalNodes() {
        DepartmentEntity parent = DepartmentEntity.builder()
                .id("dep-parent")
                .code("FIN")
                .name("Finance Dept")
                .parentId(null)
                .treeLevel(0)
                .sortOrder(1)
                .status("ACTIVE")
                .isActive(true)
                .build();

        DepartmentEntity child1 = DepartmentEntity.builder()
                .id("dep-child-1")
                .code("AUDIT")
                .name("Audit Team")
                .parentId("dep-parent")
                .treeLevel(1)
                .sortOrder(1)
                .status("ACTIVE")
                .isActive(true)
                .build();

        DepartmentEntity child2 = DepartmentEntity.builder()
                .id("dep-child-2")
                .code("TREASURY")
                .name("Treasury Team")
                .parentId("dep-parent")
                .treeLevel(1)
                .sortOrder(2)
                .status("ACTIVE")
                .isActive(true)
                .build();

        when(departmentRepository.findByIsActiveTrueOrderByTreeLevelAscSortOrderAscCodeAsc())
                .thenReturn(List.of(parent, child1, child2));

        List<DepartmentTreeResponse> tree = service.getDepartmentTree();

        assertEquals(1, tree.size());
        DepartmentTreeResponse rootNode = tree.getFirst();
        assertEquals("dep-parent", rootNode.getId());
        assertEquals("FIN", rootNode.getCode());
        assertEquals(2, rootNode.getChildren().size());
        assertEquals("AUDIT", rootNode.getChildren().get(0).getCode());
        assertEquals("TREASURY", rootNode.getChildren().get(1).getCode());
    }

    @Test
    void getDepartmentTreeReturnsThreeLevelHierarchy() {
        DepartmentEntity accounting = DepartmentEntity.builder()
                .id("dept-acc")
                .code("ACCOUNTING")
                .name("Phòng Kế Toán")
                .parentId(null)
                .ancestors(null)
                .treeLevel(0)
                .sortOrder(1)
                .status("ACTIVE")
                .isActive(true)
                .build();

        DepartmentEntity subA = DepartmentEntity.builder()
                .id("dept-sub-a")
                .code("ACC_SUB_A")
                .name("Phòng ban con A")
                .parentId("dept-acc")
                .ancestors("dept-acc")
                .treeLevel(1)
                .sortOrder(1)
                .status("ACTIVE")
                .isActive(true)
                .build();

        DepartmentEntity subB = DepartmentEntity.builder()
                .id("dept-sub-b")
                .code("ACC_SUB_B")
                .name("Phòng ban con B")
                .parentId("dept-sub-a")
                .ancestors("dept-acc,dept-sub-a")
                .treeLevel(2)
                .sortOrder(1)
                .status("ACTIVE")
                .isActive(true)
                .build();

        when(departmentRepository.findByIsActiveTrueOrderByTreeLevelAscSortOrderAscCodeAsc())
                .thenReturn(List.of(accounting, subA, subB));

        List<DepartmentTreeResponse> tree = service.getDepartmentTree();

        assertEquals(1, tree.size());
        DepartmentTreeResponse root = tree.getFirst();
        assertEquals("dept-acc", root.getId());
        assertEquals("ACCOUNTING", root.getCode());
        assertEquals(0, root.getTreeLevel());
        assertEquals(1, root.getChildren().size());

        DepartmentTreeResponse childA = root.getChildren().getFirst();
        assertEquals("dept-sub-a", childA.getId());
        assertEquals("ACC_SUB_A", childA.getCode());
        assertEquals("dept-acc", childA.getParentId());
        assertEquals(1, childA.getTreeLevel());
        assertEquals(1, childA.getChildren().size());

        DepartmentTreeResponse childB = childA.getChildren().getFirst();
        assertEquals("dept-sub-b", childB.getId());
        assertEquals("ACC_SUB_B", childB.getCode());
        assertEquals("dept-sub-a", childB.getParentId());
        assertEquals(2, childB.getTreeLevel());
        assertTrue(childB.getChildren().isEmpty());
    }

    @Test
    void getDepartmentTreeReturnsEmptyWhenNoDepartments() {
        when(departmentRepository.findByIsActiveTrueOrderByTreeLevelAscSortOrderAscCodeAsc())
                .thenReturn(List.of());

        List<DepartmentTreeResponse> tree = service.getDepartmentTree();

        assertTrue(tree.isEmpty());
    }

    private DepartmentEntity department() {
        return DepartmentEntity.builder()
                .id("dep-1")
                .code("FIN")
                .name("Finance")
                .status("ACTIVE")
                .isActive(true)
                .sortOrder(1)
                .treeLevel(0)
                .build();
    }
}
