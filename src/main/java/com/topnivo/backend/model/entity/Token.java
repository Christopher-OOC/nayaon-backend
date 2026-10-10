package com.topnivo.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(unique = true, nullable = false)
    private String token;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private Member owner;
    private boolean used;
    @CreationTimestamp
    private LocalDateTime dateCreated;
    private String usedBy;
    private LocalDateTime dateUsed;
    @Column(columnDefinition = "DECIMAL(19,2)")
    private double price;
    private String type;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Token that = (Token) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Token{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", owner=" + owner +
                ", used=" + used +
                ", dateCreated=" + dateCreated +
                ", usedBy='" + usedBy + '\'' +
                ", dateUsed=" + dateUsed +
                ", price=" + price +
                '}';
    }
}
