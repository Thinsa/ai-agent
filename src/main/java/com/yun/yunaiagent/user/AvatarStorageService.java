package com.yun.yunaiagent.user;

import org.springframework.web.multipart.MultipartFile;

/**
 * 头像存储服务抽象。
 */
public interface AvatarStorageService {

    String uploadAvatar(String username, MultipartFile file);
}
