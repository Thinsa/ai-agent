package com.yun.yunaiagent.chat;

import com.yun.yunaiagent.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 聊天会话数据访问接口。
 *
 * <p>封装按模块、会话 ID、用户维度查找或创建会话的持久化能力。</p>
 */
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByModuleAndChatIdAndUser(String module, String chatId, AppUser user);

    Optional<ChatConversation> findByModuleAndChatIdAndUserIsNull(String module, String chatId);

    Optional<ChatConversation> findByChatIdAndUser(String chatId, AppUser user);

    Optional<ChatConversation> findByChatIdAndUserIsNull(String chatId);

    List<ChatConversation> findByModuleAndUserOrderByUpdatedAtDesc(String module, AppUser user);
}
