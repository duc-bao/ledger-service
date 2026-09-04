package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.company.AssignCompanyDepartmentsRequest;
import com.ledger.ledgerservice.model.dto.request.company.CompanyStatusRequest;
import com.ledger.ledgerservice.model.dto.request.company.CreateCompanyRequest;
import com.ledger.ledgerservice.model.dto.request.company.SearchCompany;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.company.CompanyDepartmentAssignmentResponse;
import com.ledger.ledgerservice.model.dto.response.company.CompanyResponse;
import com.ledger.ledgerservice.model.dto.response.company.CompanyTreeResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.company.CompanyDepartmentService;
import com.ledger.ledgerservice.service.company.CompanyService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/companies")
@RequiredArgsConstructor
@Tag(name = "Company Admin", description = "APIs for company creation and tree management")
@SecurityRequirement(name = "bearerAuth")
public class CompanyController {
    private final CompanyService companyService;
    private final CompanyDepartmentService companyDepartmentService;
    private final ResponseHelper responseHelper;

    @PostMapping
    @Operation(summary = "Create company")
    public ResponseEntity<BaseResponse<CompanyResponse>> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse data = companyService.createCompany(request);
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.CREATED);
    }

    @GetMapping("/{companyId}")
    @Operation(summary = "Get company by id")
    public ResponseEntity<BaseResponse<CompanyResponse>> getCompanyById(@PathVariable String companyId) {
        CompanyResponse data = companyService.getById(companyId);
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.OK);
    }

    @PostMapping("/search")
    @Operation(summary = "Search company tree")
    public ResponseEntity<BaseResponse<List<CompanyTreeResponse>>> search(@RequestBody(required = false) SearchCompany request) {
        List<CompanyTreeResponse> data = companyService.search(request);
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.OK);
    }

    @GetMapping("/tree")
    @Operation(summary = "Get full company tree")
    public ResponseEntity<BaseResponse<List<CompanyTreeResponse>>> getTree() {
        List<CompanyTreeResponse> data = companyService.getCompanyTree();
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.OK);
    }

    @PutMapping("/{companyId}")
    @Operation(summary = "Update company")
    public ResponseEntity<BaseResponse<CompanyResponse>> updateCompany(@PathVariable String companyId,
                                                                       @Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse data = companyService.updateCompany(companyId, request);
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.OK);
    }

    @PutMapping("/{companyId}/departments")
    @Operation(summary = "Assign departments to company")
    public ResponseEntity<BaseResponse<CompanyDepartmentAssignmentResponse>> assignDepartments(@PathVariable String companyId,
                                                                                                @Valid @RequestBody AssignCompanyDepartmentsRequest request) {
        CompanyDepartmentAssignmentResponse data = companyDepartmentService.assignDepartments(companyId, request);
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.OK);
    }

    @PatchMapping("/{companyId}/status")
    @Operation(summary = "Lock or activate company")
    public ResponseEntity<BaseResponse<CompanyResponse>> changeStatus(@PathVariable String companyId,
                                                                      @Valid @RequestBody CompanyStatusRequest request) {
        CompanyResponse data = companyService.changeStatus(companyId, request.getStatus());
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.OK);
    }
}
