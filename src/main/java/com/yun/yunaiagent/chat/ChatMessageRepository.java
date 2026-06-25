package com.yun.yunaiagent.chat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 聊天消息数据访问接口。
 *
 * <p>用于按会话查询历史消息，并支撑最近消息窗口、聊天记录列表等功能。</p>
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationOrderByCreatedAtAsc(ChatConversation conversation);

    List<ChatMessage> findByConversationOrderByCreatedAtDesc(ChatConversation conversation, Pageable pageable);

    void deleteByConversation(ChatConversation conversation);
}
