package com.topnivo.backend.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@Entity
@Table(name = "token_order_items")
public class TokenOrderItem  extends OrderItem {
}
