package com.yun.yunaiagent.background;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 用户背景数据访问接口。
 *
 * <p>按用户维度查询和保存背景配置。</p>
 */
public interface UserBackgroundRepository extends JpaRepository<UserBackground, Long> {

    List<UserBackground> findByUserId(Long userId);

    Optional<UserBackground> findByUserIdAndAgentKey(Long userId, String agentKey);

    void deleteByUserIdAndAgentKey(Long userId, String agentKey);
}
