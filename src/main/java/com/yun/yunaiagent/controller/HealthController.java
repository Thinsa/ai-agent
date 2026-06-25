package com.yun.yunaiagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
/**
 * 健康检查接口。
 *
 * <p>用于前端、部署平台或反向代理快速确认后端服务是否可用。</p>
 */
public class HealthController {

    /**
     * 健康检查接口，供部署平台、网关或本地调试确认服务是否存活。
     */
    @GetMapping
    public String healthCheck() {
        return "ok";
    }
}
