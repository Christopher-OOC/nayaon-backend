package com.topnivo.backend.service;

import com.topnivo.backend.exception.exception.ErrorMessages;
import com.topnivo.backend.model.entity.Member;
import com.topnivo.backend.repository.MemberRepository;
import com.topnivo.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserDetailsManager implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsernameIgnoreCase(username);
        if (Objects.isNull(member)) {
            throw new UsernameNotFoundException(ErrorMessages.NO_SUCH_MEMBER);
        }

        return new CustomUserDetails(member);
    }
}
