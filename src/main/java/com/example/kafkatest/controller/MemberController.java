package com.example.kafkatest.controller;

import com.example.kafkatest.dto.RegisterDto;
import com.example.kafkatest.entity.Member;
import com.example.kafkatest.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberController {
    private final MemberRepository memberRepository;

    @PostMapping("/register")
    public void register(@RequestBody RegisterDto.RegisterRequest request) {
        Member member = Member.builder()
                        .memberVo(request.toMemberVo()).build();

        memberRepository.save(member);
    }
}
