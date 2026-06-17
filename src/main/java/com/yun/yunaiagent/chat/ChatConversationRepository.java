package com.yun.yunaiagent.chat;

import com.yun.yunaiagent.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByModuleAndChatId(String module, String chatId);

    Optional<ChatConversation> findByChatId(String chatId);

    List<ChatConversation> findByModuleAndUserOrderByUpdatedAtDesc(String module, AppUser user);
}
