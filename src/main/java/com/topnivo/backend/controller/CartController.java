package com.topnivo.backend.controller;

import com.topnivo.backend.mapper.CartMapper;
import com.topnivo.backend.mapper.StoreMapper;
import com.topnivo.backend.model.entity.Cart;
import com.topnivo.backend.model.request.CartItemUpdateRequest;
import com.topnivo.backend.model.request.CartItemCreateRequest;
import com.topnivo.backend.model.response.ApiResponse;
import com.topnivo.backend.model.response.CartResponse;
import com.topnivo.backend.model.response.ResponseStatus;
import com.topnivo.backend.service.CartService;
import com.topnivo.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartMapper cartMapper;
    private final StoreMapper storeMapper;

    @PostMapping(value = "/add-to-cart")
    public ResponseEntity<?> addProductToCart(
            @RequestBody CartItemCreateRequest request
    ) {
        String memberId = SecurityUtils.getCurrentMemberId();
        Cart cart = cartService.addProductToCart(memberId, request);
        CartResponse cartResponse = cartMapper.cartToCartResponse(cart);
        ApiResponse<CartResponse> response = new ApiResponse<>(
                ResponseStatus.ADDED.name(),
                "Product added to your cart successfully!",
                cartResponse,
                null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "")
    public ResponseEntity<?> findCartByMemberId() {
        String memberId = SecurityUtils.getCurrentMemberId();
        Cart cart = cartService.findCartByMemberId(memberId);
        CartResponse cartResponse = cartMapper.cartToCartResponse(cart);
        ApiResponse<CartResponse> response = new ApiResponse<>(
                ResponseStatus.SUCCESS.name(),
                "Cart item retrieve successfully!",
                cartResponse,
                null
        );

        return ResponseEntity.ok(response);
    }

    // Update Cart OrderItem
    @PutMapping(value = "")
    public ResponseEntity<?> updateCartItemQuantity(
            @RequestBody List<CartItemUpdateRequest> request
    ) {
        String memberId = SecurityUtils.getCurrentMemberId();
        Cart cart = cartService.updateCartItemQuantity(memberId, request);
        CartResponse cartResponse = cartMapper.cartToCartResponse(cart);
        ApiResponse<CartResponse> response = new ApiResponse<>(
                ResponseStatus.ADDED.name(),
                "Cart item updated successfully!",
                cartResponse,
                null
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "")
    public ResponseEntity<?> deleteCartItem(
            @RequestParam("cartItemId") int cartItemId
    ) {
        String memberId = SecurityUtils.getCurrentMemberId();
        Cart cart = cartService.deleteCartItemFromCart(memberId, cartItemId);
        CartResponse cartResponse = cartMapper.cartToCartResponse(cart);
        ApiResponse<CartResponse> response = new ApiResponse<>(
                ResponseStatus.DELETED.name(),
                "Cart item deleted successfully!",
                cartResponse,
                null
        );

        return ResponseEntity.ok(response);
    }
}
