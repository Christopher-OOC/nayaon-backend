package com.topnivo.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.topnivo.backend.model.entity.Member;
import com.topnivo.backend.model.request.PackageCreateRequest;
import com.topnivo.backend.model.request.TokenCreateRequest;
import com.topnivo.backend.model.response.ApiResponse;
import com.topnivo.backend.model.response.ResponseStatus;
import com.topnivo.backend.model.response.TokenResponse;
import com.topnivo.backend.service.TokenService;
import com.topnivo.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/tokens")
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/purchase")
    public ResponseEntity<?> createToken(@RequestParam(value = "data", required = true) String requestBody, @RequestParam(value = "file", required = false) MultipartFile multipartFile) throws JsonProcessingException {
        TokenCreateRequest request = objectMapper.readValue(requestBody, TokenCreateRequest.class);
        tokenService.createToken(request, multipartFile);
        ApiResponse<String> response = new ApiResponse<>(
                ResponseStatus.CREATED.name(),
                "Token created successfully!",
                "Token created successfully!",
                null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getTokensByPage(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size
    ) {
        Member currentMember = SecurityUtils.getCurrentMember();
        Page<TokenResponse> tokenPage = tokenService.getTokensByPage(currentMember, page, size);
        ApiResponse<List<TokenResponse>> response = new ApiResponse<>(
                ResponseStatus.SUCCESS.name(),
                "Tokens retrieved successfully!",
                tokenPage.getContent(),
                buildMetadata(page, size, tokenPage)
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/unused")
    public ResponseEntity<?> getUnusedTokensByPage(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size
    ) {
        Page<TokenResponse> tokenPage = tokenService.getUnusedTokensByPage(page, size);
        ApiResponse<List<TokenResponse>> response = new ApiResponse<>(
                ResponseStatus.SUCCESS.name(),
                "Unused tokens retrieved successfully!",
                tokenPage.getContent(),
                buildMetadata(page, size, tokenPage)
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/mine")
    public ResponseEntity<?> getTokensOfCurrentMemberByPage(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size
    ) {
        Page<TokenResponse> tokenPage = tokenService.getTokensOfCurrentMemberByPage(page, size);
        ApiResponse<List<TokenResponse>> response = new ApiResponse<>(
                ResponseStatus.SUCCESS.name(),
                "Your tokens retrieved successfully!",
                tokenPage.getContent(),
                buildMetadata(page, size, tokenPage)
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{tokenId}")
    public ResponseEntity<?> findTokenById(@PathVariable("tokenId") int tokenId) {
        TokenResponse token = tokenService.findTokenById(tokenId);
        ApiResponse<TokenResponse> response = new ApiResponse<>(
                ResponseStatus.SUCCESS.name(),
                "Token retrieved successfully!",
                token,
                null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/value/{token}")
    public ResponseEntity<?> findTokenByValue(@PathVariable("token") String token) {
        TokenResponse tokenResponse = tokenService.findTokenByValue(token);
        ApiResponse<TokenResponse> response = new ApiResponse<>(
                ResponseStatus.SUCCESS.name(),
                "Token retrieved successfully!",
                tokenResponse,
                null
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/use/{token}")
    public ResponseEntity<?> useToken(@PathVariable("token") String token) {
        TokenResponse tokenResponse = tokenService.useToken(token);
        ApiResponse<TokenResponse> response = new ApiResponse<>(
                ResponseStatus.UPDATED.name(),
                "Token used successfully!",
                tokenResponse,
                null
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{tokenId}")
    public ResponseEntity<?> deleteToken(@PathVariable("tokenId") int tokenId) {
        tokenService.deleteToken(tokenId);
        ApiResponse<Object> response = new ApiResponse<>(
                ResponseStatus.DELETED.name(),
                "Token deleted successfully!",
                null,
                null
        );

        return ResponseEntity.ok(response);
    }

    private Map<String, Integer> buildMetadata(int page, int size, Page<TokenResponse> tokenPage) {
        Map<String, Integer> metadata = new LinkedHashMap<>();
        metadata.put("page", page);
        metadata.put("size", size);
        metadata.put("numberOfElements", tokenPage.getNumberOfElements());
        metadata.put("totalNumberOfElements", (int) tokenPage.getTotalElements());
        metadata.put("totalPages", tokenPage.getTotalPages());

        return metadata;
    }
}
