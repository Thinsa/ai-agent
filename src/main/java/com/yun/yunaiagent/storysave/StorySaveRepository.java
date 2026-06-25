package com.yun.yunaiagent.storysave;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 剧情存档数据访问接口。
 */
public interface StorySaveRepository extends JpaRepository<StorySave, Long> {

    List<StorySave> findByUserIdAndStoryIdOrderBySavedAtDesc(Long userId, String storyId);

    void deleteByUserIdAndId(Long userId, Long id);
}
