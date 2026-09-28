package com.topnivo.backend.mapper;

import com.topnivo.backend.model.entity.Token;
import com.topnivo.backend.model.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TokenMapper {

    private final ModelMapper modelMapper;

    public TokenResponse tokenToResponse(Token token) {
        if (Objects.isNull(token)) {
            return null;
        }

        TokenResponse tokenResponse = modelMapper.map(token, TokenResponse.class);
        if (!Objects.isNull(token.getOwner())) {
            tokenResponse.setOwnerId(token.getOwner().getMemberId());
            tokenResponse.setOwnerUsername(token.getOwner().getUsername());
        }

        return tokenResponse;
    }

    public List<TokenResponse> tokenToResponse(List<Token> tokens) {
        return tokens.stream().map(this::tokenToResponse).toList();
    }
}
