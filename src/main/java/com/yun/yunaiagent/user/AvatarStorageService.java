package com.yun.yunaiagent.user;

import org.springframework.web.multipart.MultipartFile;

public interface AvatarStorageService {

    String uploadAvatar(String username, MultipartFile file);
}
