package com.example.kafkatest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MakeChatRoomRequest {
    private String roomName;
    private int numberOfMember;
    private List<String> userId;
}
