package com.topnivo.backend.service;

import com.topnivo.backend.exception.exception.BadRequestException;
import com.topnivo.backend.exception.exception.ErrorMessages;
import com.topnivo.backend.exception.exception.NoSuchResourceException;
import com.topnivo.backend.mapper.TokenMapper;
import com.topnivo.backend.model.entity.Member;
import com.topnivo.backend.model.entity.Token;
import com.topnivo.backend.model.request.TokenCreateRequest;
import com.topnivo.backend.model.response.TokenResponse;
import com.topnivo.backend.repository.TokenRepository;
import com.topnivo.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private static final String TOKEN_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int TOKEN_LENGTH = 12;
    private static final Random RANDOM = new SecureRandom();

    private final TokenRepository tokenRepository;
    private final TokenMapper tokenMapper;
    private final MemberService memberService;

    public TokenResponse createToken(TokenCreateRequest request) {

        if (request.getPrice() <= 0) {
            throw new BadRequestException(ErrorMessages.INVALID_TOKEN_PRICE);
        }

        Member owner = SecurityUtils.getCurrentMember();
        Token token = Token
                .builder()
                .token(generateTokenValue())
                .owner(owner)
                .used(false)
                .price(request.getPrice())
                .build();

        return tokenMapper.tokenToResponse(tokenRepository.save(token));
    }

    public TokenResponse useToken(String tokenValue) {
        Token token = getTokenByValue(tokenValue);

        if (token.isUsed()) {
            throw new BadRequestException(ErrorMessages.TOKEN_ALREADY_USED);
        }

        Member currentMember = SecurityUtils.getCurrentMember();
        if (token.getOwner().getId() == currentMember.getId()) {
            throw new BadRequestException(ErrorMessages.CANNOT_USE_OWN_TOKEN);
        }

        token.setUsed(true);
        token.setUsedBy(currentMember.getUsername());
        token.setDateUsed(LocalDateTime.now());

        return tokenMapper.tokenToResponse(tokenRepository.save(token));
    }

    public void deleteToken(int tokenId) {

        tokenRepository.delete(getTokenById(tokenId));
    }

    public TokenResponse findTokenById(int tokenId) {
        return tokenMapper.tokenToResponse(getTokenById(tokenId));
    }

    public TokenResponse findTokenByValue(String tokenValue) {
        return tokenMapper.tokenToResponse(getTokenByValue(tokenValue));
    }

    public Page<TokenResponse> getTokensByPage(Member currentMember, int page, int size) {

        return tokenRepository.findByOwnerOrderByDateCreatedDesc(currentMember, PageRequest.of(page - 1, size))
                .map(tokenMapper::tokenToResponse);
    }

    public Page<TokenResponse> getUnusedTokensByPage(int page, int size) {

        return tokenRepository.findByUsedOrderByDateCreatedDesc(false, PageRequest.of(page - 1, size))
                .map(tokenMapper::tokenToResponse);
    }

    public Page<TokenResponse> getTokensOfCurrentMemberByPage(int page, int size) {
        Member currentMember = SecurityUtils.getCurrentMember();

        return tokenRepository.findByOwnerOrderByDateCreatedDesc(currentMember, PageRequest.of(page - 1, size))
                .map(tokenMapper::tokenToResponse);
    }

    private Token getTokenById(int tokenId) {
        return tokenRepository.findById(tokenId)
                .orElseThrow(() -> new NoSuchResourceException(ErrorMessages.NO_SUCH_TOKEN));
    }

    private Token getTokenByValue(String tokenValue) {
        return Optional.ofNullable(tokenRepository.findByToken(tokenValue))
                .orElseThrow(() -> new NoSuchResourceException(ErrorMessages.NO_SUCH_TOKEN));
    }

    private String generateTokenValue() {
        String tokenValue;
        do {
            StringBuilder builder = new StringBuilder(TOKEN_LENGTH);
            for (int i = 0; i < TOKEN_LENGTH; i++) {
                builder.append(TOKEN_CHARACTERS.charAt(RANDOM.nextInt(TOKEN_CHARACTERS.length())));
            }
            tokenValue = builder.toString();
        } while (tokenRepository.existsByToken(tokenValue));

        return tokenValue;
    }

    private void checkIfAdminIsValid() {
        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        boolean isAdminSuperAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        String adminUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        Member admin = memberService.findMemberByUsername(adminUserName);

        if (!isAdminSuperAdmin && admin.getSponsor() == null && admin.getPlacer() == null) {
            throw new BadRequestException(ErrorMessages.NO_SPONSOR_AND_PLACER);
        }
    }
}
