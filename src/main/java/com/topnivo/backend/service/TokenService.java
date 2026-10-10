package com.topnivo.backend.service;

import com.topnivo.backend.exception.exception.BadRequestException;
import com.topnivo.backend.exception.exception.ErrorMessages;
import com.topnivo.backend.exception.exception.NoSuchResourceException;
import com.topnivo.backend.mapper.TokenMapper;
import com.topnivo.backend.model.constant.EventType;
import com.topnivo.backend.model.constant.OrderStatus;
import com.topnivo.backend.model.constant.OrderType;
import com.topnivo.backend.model.entity.*;
import com.topnivo.backend.model.entity.Package;
import com.topnivo.backend.model.request.TokenCreateRequest;
import com.topnivo.backend.model.response.TokenResponse;
import com.topnivo.backend.repository.*;
import com.topnivo.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

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
    private final FileService fileService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EventRepository eventRepository;
    private final PackageRepository packageRepository;

    @Transactional
    public void createToken(TokenCreateRequest request, MultipartFile multipartFile) {
        if (request.getQuantity() <= 0) {
            throw new BadRequestException(ErrorMessages.INVALID_TOKEN_PRICE);
        }

        Package aPackage = packageRepository.findById(request.getPackageId());
        if (aPackage == null) {
            throw new NoSuchResourceException(ErrorMessages.NO_SUCH_PACKAGE);
        }

        Member owner = SecurityUtils.getCurrentMember();

        Order order = new Order();
        order.setMember(owner);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderType(OrderType.TOKEN_PURCHASE);
        order.setTotalAmount(aPackage.getPrice() * request.getQuantity());
        order.setOrderId(UUID.randomUUID().toString());

        order = orderRepository.save(order);

        TokenOrderItem orderItem = new TokenOrderItem();
        orderItem.setOrder(order);
        orderItem.setName("Token Purchase");
        orderItem.setDescription("You purchase %s units of token for %s per token.".formatted(request.getQuantity(), aPackage.getPrice()));
        orderItem.setPrice(aPackage.getPrice() * request.getQuantity());
        orderItem.setOriginalId(request.getPackageId() + "");
        orderItem.setQuantity(request.getQuantity());

        if (multipartFile != null && !multipartFile.isEmpty()) {
            try {
                String imageString = fileService.fileBtyeToGenerateFileUrl(multipartFile.getBytes());
                orderItem.setImage(imageString);
            }
            catch (Exception ex) {
                throw new RuntimeException("File corrupted");
            }
        }

        Event tokenPurchaseEvent = new Event();
        tokenPurchaseEvent.setAcknowledged(false);
        tokenPurchaseEvent.setMessage("%s just bought %d token now".formatted(owner.getUsername(), request.getQuantity()));
        tokenPurchaseEvent.setEventDate(LocalDateTime.now());
        tokenPurchaseEvent.setType(EventType.TOKEN_PURCHASE);

        orderItemRepository.save(orderItem);
        eventRepository.save(tokenPurchaseEvent);
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
