package com.yun.yunaiagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检查接口，供部署平台、网关或本地调试确认服务是否存活。
     */
    @GetMapping
    public String healthCheck() {
        return "ok";
    }
}
