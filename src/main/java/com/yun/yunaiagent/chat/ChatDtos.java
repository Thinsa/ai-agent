package com.yun.yunaiagent.chat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天模块 DTO 集合。
 *
 * <p>定义会话列表、消息详情、历史记录等聊天接口的传输对象。</p>
 */
public final class ChatDtos {

    private ChatDtos() {
    }

    /**
     * 会话列表摘要。
     */
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

    /**
     * 单条聊天消息响应。
     */
    public record MessageResponse(String role, String content, LocalDateTime createdAt) {
        public static MessageResponse from(ChatMessage message) {
            return new MessageResponse(message.getRole(), message.getContent(), message.getCreatedAt());
        }
    }

    /**
     * 会话详情响应。
     */
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
