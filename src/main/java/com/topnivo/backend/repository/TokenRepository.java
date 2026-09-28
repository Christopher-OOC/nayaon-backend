package com.topnivo.backend.repository;

import com.topnivo.backend.model.entity.Member;
import com.topnivo.backend.model.entity.Token;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TokenRepository extends JpaRepository<Token, Integer>, PagingAndSortingRepository<Token, Integer> {

    Token findByToken(String token);

    boolean existsByToken(String token);

    List<Token> findByOwner(Member owner);

    Page<Token> findByOwnerOrderByDateCreatedDesc(Member owner, Pageable pageable);

    Page<Token> findByUsedOrderByDateCreatedDesc(boolean used, Pageable pageable);

}
