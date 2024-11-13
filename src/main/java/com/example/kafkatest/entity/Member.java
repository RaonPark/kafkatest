package com.example.kafkatest.entity;

import com.example.kafkatest.support.BaseTimeEntity;
import com.example.kafkatest.support.RandomNumberGenerator;
import com.example.kafkatest.vo.MemberVo;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
// Entity로 지정된 클래스는 항상 pub혹은 protected no-args constructor가 있어야 한다.
@NoArgsConstructor
@Table(name = "MEMBER")
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "member_id")
    private UUID id;

    @Column
    private String userId;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String email;

    @Column
    private String address;

    @Column(nullable = false, length = 4)
    private String uniqueNumber;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<ChatroomMember> chatroomMembers = new HashSet<>();

    @Builder
    private Member(MemberVo memberVo) {
        userId = memberVo.getUserId();
        username = memberVo.getUsername();
        address = memberVo.getAddress();
        email = memberVo.getEmail();
        password = memberVo.getPassword();
        uniqueNumber = RandomNumberGenerator.generateRandom4Numbers();
    }
}
