package com.example.kafkatest.dto.request.chat;

import com.example.kafkatest.support.ChatMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class SendChatMessageRequest {
    private ChatMessageType chatMessageType;
    private String image;
    private String message;
    private String emoticon;
    private long chatRoomId;
    private int order;
}
