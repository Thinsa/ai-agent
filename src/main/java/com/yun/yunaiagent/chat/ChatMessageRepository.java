package com.yun.yunaiagent.chat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationOrderByCreatedAtAsc(ChatConversation conversation);

    List<ChatMessage> findByConversationOrderByCreatedAtDesc(ChatConversation conversation, Pageable pageable);
}
