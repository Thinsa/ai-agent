package com.yun.yunaiagent.chat;

import java.time.LocalDateTime;
import java.util.List;

public final class ChatDtos {

    private ChatDtos() {
    }

    public record SessionSummary(String module, String chatId, String title, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static SessionSummary from(ChatConversation conversation) {
            return new SessionSummary(
                    conversation.getModule(),
                    conversation.getChatId(),
                    conversation.getTitle(),
                    conversation.getCreatedAt(),
                    conversation.getUpdatedAt()
            );
        }
    }

    public record MessageResponse(String role, String content, LocalDateTime createdAt) {
        public static MessageResponse from(ChatMessage message) {
            return new MessageResponse(message.getRole(), message.getContent(), message.getCreatedAt());
        }
    }

    public record ConversationDetail(String module, String chatId, String title, List<MessageResponse> messages) {
        public static ConversationDetail from(ChatConversation conversation, List<ChatMessage> messages) {
            return new ConversationDetail(
                    conversation.getModule(),
                    conversation.getChatId(),
                    conversation.getTitle(),
                    messages.stream().map(MessageResponse::from).toList()
            );
        }
    }
}
