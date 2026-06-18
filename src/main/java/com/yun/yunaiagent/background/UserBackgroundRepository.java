package com.yun.yunaiagent.background;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBackgroundRepository extends JpaRepository<UserBackground, Long> {

    List<UserBackground> findByUserId(Long userId);

    Optional<UserBackground> findByUserIdAndAgentKey(Long userId, String agentKey);

    void deleteByUserIdAndAgentKey(Long userId, String agentKey);
}
