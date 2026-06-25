package com.yun.yunaiagent.storysave;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
/**
 * 剧情存档业务服务。
 */
public class StorySaveService {

    private final StorySaveRepository repository;

    public StorySaveService(StorySaveRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public StorySave createSave(Long userId, String storyId, String sceneId, String choiceHistory) {
        StorySave save = StorySave.create(userId, storyId, sceneId, choiceHistory);
        return repository.save(save);
    }

    @Transactional(readOnly = true)
    public List<StorySave> listSaves(Long userId, String storyId) {
        return repository.findByUserIdAndStoryIdOrderBySavedAtDesc(userId, storyId);
    }

    @Transactional
    public void deleteSave(Long userId, Long saveId) {
        repository.deleteByUserIdAndId(userId, saveId);
    }
}
