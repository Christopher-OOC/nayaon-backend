package com.topnivo.backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {
    private int id;
    private String token;
    private String ownerId;
    private String ownerUsername;
    private boolean used;
    private LocalDateTime dateCreated;
    private String usedBy;
    private LocalDateTime dateUsed;
    private double price;
}
