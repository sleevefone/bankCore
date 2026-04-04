package com.payhub.bankcore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "服务健康检查接口")
public class HealthController {

    @GetMapping("/internal/health")
    @Operation(summary = "健康检查", description = "返回 bank-core 服务当前健康状态，供本地联调或运维探活使用。")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "bank-core");
    }
}
