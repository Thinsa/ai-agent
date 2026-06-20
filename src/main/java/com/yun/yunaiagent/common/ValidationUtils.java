package com.yun.yunaiagent.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 跨服务复用的参数校验与字符串工具方法。
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    /**
     * 校验值非空，否则抛出 400 错误。
     */
    public static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value.trim();
    }

    /**
     * 判断字符串是否为 null 或空白。
     */
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 规范化字符串：null 或空白返回回退值，否则 trim。
     */
    public static String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
