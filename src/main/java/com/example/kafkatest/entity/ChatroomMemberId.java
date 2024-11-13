package com.example.kafkatest.entity;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@NoArgsConstructor
@Data
public class ChatroomMemberId implements Serializable {
    private Long chatroomId;
    private UUID memberId;

    @Builder
    protected ChatroomMemberId(Long chatroomId, UUID memberId) {
        this.chatroomId = chatroomId;
        this.memberId = memberId;
    }
}
