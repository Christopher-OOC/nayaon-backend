package com.topnivo.backend.controller;

import com.topnivo.backend.mapper.MemberMapper;
import com.topnivo.backend.model.entity.AdminSetting;
import com.topnivo.backend.model.entity.Member;
import com.topnivo.backend.model.entity.Order;
import com.topnivo.backend.model.request.AdminSettingUpdateRequest;
import com.topnivo.backend.model.request.MemberUpdateRequest;
import com.topnivo.backend.model.response.ApiResponse;
import com.topnivo.backend.model.response.MemberResponse;
import com.topnivo.backend.model.response.OrderResponse;
import com.topnivo.backend.model.response.ResponseStatus;
import com.topnivo.backend.service.AdminSettingService;
import com.topnivo.backend.service.MemberService;
import com.topnivo.backend.service.OrderService;
import com.topnivo.backend.util.SecurityUtils;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/admins")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminSettingService adminSettingService;
    private final MemberService memberService;
    private final MemberMapper memberMapper;
    private final ModelMapper modelMapper;
    private final OrderService orderService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping(value = "/{memberId}/members")
    public ResponseEntity<?> updateMemberRoleAndEnable(
            @PathVariable("memberId") String memberId,
            @RequestParam(value = "role", required = false, defaultValue = "") String role,
            @RequestParam(value = "enabled", required = true) boolean enabled
    ) {
        Member member = memberService.adminUpdateMember(memberId, role, enabled);
        MemberResponse memberResponse = memberMapper.memberToResponse(member);
        ApiResponse<MemberResponse> response = new ApiResponse<>(
                ResponseStatus.UPDATED.name(),
                "Admin has updated member successfully!",
                memberResponse,
                null
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping(value = "/update-member-info/{memberId}")
    public ResponseEntity<?> updateMemberInfo(
            @PathVariable("memberId") String memberId,
            @RequestBody MemberUpdateRequest request
            ) {
        Member member = memberService.adminUpdateMemberInfo(memberId, request);
        MemberResponse memberResponse = memberMapper.memberToResponse(member);
        ApiResponse<MemberResponse> response = new ApiResponse<>(
                ResponseStatus.UPDATED.name(),
                "Admin has updated member successfully!",
                memberResponse,
                null
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping(value = "/settings")
    public ResponseEntity<?> updateAdminSettings(
            @RequestBody List<AdminSettingUpdateRequest> request
    ) {
        List<AdminSetting> settings = adminSettingService.updateAdminSettings(request);
        ApiResponse<List<AdminSetting>> response = new ApiResponse<>(
                ResponseStatus.UPDATED.name(),
                "Admin setting updated successfully!",
                settings,
                null
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping(value = "/settings")
    public ResponseEntity<?> findAllAdminSettings() {
        List<AdminSetting> settings = adminSettingService.findAllAdminSettings();
        ApiResponse<List<AdminSetting>> response = new ApiResponse<>(
                ResponseStatus.SUCCESS.name(),
                "Admin settings retrieved successfully!",
                settings,
                null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/settings/{settingId}")
    public ResponseEntity<?> findAdminSettingById(@PathVariable("settingId") int settingId) {
        AdminSetting setting = adminSettingService.findAdminSetting(settingId);
        ApiResponse<AdminSetting> response = new ApiResponse<>(
                ResponseStatus.SUCCESS.name(),
                "Admin setting retrieved successfully!",
                setting,
                null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/settings/name/{name}")
    public ResponseEntity<?> findAdminSettingByName(@PathVariable("name") String name) {
        AdminSetting setting = adminSettingService.findAdminSettingByName(name);
        ApiResponse<AdminSetting> response = new ApiResponse<>(
                ResponseStatus.SUCCESS.name(),
                "Admin setting retrieved successfully!",
                setting,
                null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/login-as-user/{memberId}/{adminId}")
    public ResponseEntity<?> adminLoginAsUser(
        @PathVariable("memberId") String memberId,
        @PathVariable("adminId") String adminId
    ) {
        Map<String, Object> data = memberService.loginAsUser(memberId, adminId);
        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                ResponseStatus.SUCCESS.name(),
                "Admin setting retrieved successfully!",
                data,
                null
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @PutMapping(value = "/{memberId}/activate-package/{packageId}")
    public ResponseEntity<?> adminActivateUserPackage(
            @PathVariable("memberId") String memberId,
            @PathVariable("packageId") int packageId
    ) throws MessagingException {
        Order order = memberService.adminActivateUserPackage(memberId, packageId);
        Member member = memberService.confirmOrderById(memberId, order.getOrderId(), "CONFIRMED");
        MemberResponse memberResponse = memberMapper.memberToResponse(member);
        ApiResponse<MemberResponse> response = new ApiResponse<>(
                ResponseStatus.ACTIVATED.name(),
                "Package activated successfully!",
                memberResponse,
                null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/orders")
    public ResponseEntity<?> findAdminOrdersPage(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "5") int size
    ) {
        Page<Order> orderPage = orderService.findAdminOrdersPage(page, size);

        Map<String, Integer> metadata = new LinkedHashMap<>();
        metadata.put("page", page);
        metadata.put("size", size);
        metadata.put("number", orderPage.getNumberOfElements());
        metadata.put("totalPages", orderPage.getTotalPages());

        List<OrderResponse> responseList = new ArrayList<>();
        orderPage.getContent().forEach(order -> responseList.add(modelMapper.map(order, OrderResponse.class)));

        ApiResponse<List<OrderResponse>> response = new ApiResponse<>(
                ResponseStatus.SUCCESS.name(),
                "Orders retrieved successfully!",
                responseList,
                metadata
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/orders/{orderId}")
    public ResponseEntity<?> changeOrderStatus(
            @PathVariable(value = "orderId") String orderId
    ) {
        Order order = orderService.findOrderById(orderId);
        OrderResponse orderResponse = modelMapper.map(order, OrderResponse.class);
        ApiResponse<OrderResponse> response = new ApiResponse<>(
                ResponseStatus.SUCCESS.name(),
                "Order retrieved successfully!",
                orderResponse,
                null
        );

        return ResponseEntity.ok(response);
    }

    //@PreAuthorize("hasAnyRole('SUPER_ADMIN, ADMIN')")
    @PutMapping(value = "/treat-order/{orderId}")
    public ResponseEntity<?> adminTreatOrder(
            @PathVariable("orderId") String orderId,
            @RequestParam(value = "status") String status
    ) throws MessagingException {
        String memberId = SecurityUtils.getCurrentMemberId();
        Member member = memberService.confirmOrderById(memberId, orderId, status);
        MemberResponse memberResponse = memberMapper.memberToResponse(member);
        ApiResponse<MemberResponse> response = new ApiResponse<>(
                ResponseStatus.ACTIVATED.name(),
                "Order treated successfully!",
                memberResponse,
                null
        );

        return ResponseEntity.ok(response);
    }
}
