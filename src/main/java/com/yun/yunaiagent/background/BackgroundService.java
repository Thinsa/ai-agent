package com.yun.yunaiagent.background;

import com.yun.yunaiagent.tools.OssObjectStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
/**
 * 用户背景业务服务。
 *
 * <p>封装背景配置的读取、保存和默认值兜底逻辑。</p>
 */
public class BackgroundService {

    private static final String BG_DIR = "backgrounds";

    private final UserBackgroundRepository repository;

    private final OssObjectStorageService ossService;

    public BackgroundService(UserBackgroundRepository repository, OssObjectStorageService ossService) {
        this.repository = repository;
        this.ossService = ossService;
    }

    @Transactional(readOnly = true)
    public Map<String, BackgroundDto> listBackgrounds(Long userId) {
        List<UserBackground> backgrounds = repository.findByUserId(userId);
        Map<String, BackgroundDto> result = new LinkedHashMap<>();
        for (UserBackground bg : backgrounds) {
            result.put(bg.getAgentKey(), toDto(bg));
        }
        return result;
    }

    @Transactional
    public BackgroundDto saveBackground(Long userId, String agentKey, byte[] imageBytes, String contentType, double opacity) {
        String objectKey = BG_DIR + "/" + userId + "/" + agentKey + "/" + UUID.randomUUID();
        String imageUrl = ossService.upload(objectKey, imageBytes, contentType);

        UserBackground bg = repository.findByUserIdAndAgentKey(userId, agentKey)
                .orElseGet(() -> UserBackground.create(userId, agentKey, imageUrl, opacity));
        bg.update(imageUrl, opacity);
        return toDto(repository.save(bg));
    }

    @Transactional
    public BackgroundDto updateOpacity(Long userId, String agentKey, double opacity) {
        UserBackground bg = repository.findByUserIdAndAgentKey(userId, agentKey)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Background not found"));
        bg.update(bg.getImageData(), opacity);
        return toDto(repository.save(bg));
    }

    @Transactional
    public void deleteBackground(Long userId, String agentKey) {
        repository.deleteByUserIdAndAgentKey(userId, agentKey);
    }

    private BackgroundDto toDto(UserBackground bg) {
        return new BackgroundDto(bg.getAgentKey(), bg.getImageData(), bg.getOpacity());
    }

    /**
     * 用户背景展示数据。
     */
    public record BackgroundDto(String agentKey, String imageData, double opacity) {
    }
}
