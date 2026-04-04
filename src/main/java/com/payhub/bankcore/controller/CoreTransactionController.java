package com.payhub.bankcore.controller;

import com.payhub.bankcore.application.dto.CoreTransactionResponse;
import com.payhub.bankcore.application.dto.CreateCoreTransactionRequest;
import com.payhub.bankcore.application.service.CoreTransactionApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Validated
@RestController
@RequestMapping("/core/transactions")
@Tag(name = "Core Transaction", description = "核心交易记账与查单接口")
public class CoreTransactionController {

    private final CoreTransactionApplicationService applicationService;

    public CoreTransactionController(CoreTransactionApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @Operation(summary = "创建核心交易", description = "接收支付侧记账请求，完成核心交易主单、分录、余额和审计日志处理。")
    public CoreTransactionResponse create(@Valid @RequestBody CreateCoreTransactionRequest request) {
        return applicationService.create(request);
    }

    @GetMapping("/{bizOrderId}")
    @Operation(summary = "按业务订单号查单", description = "根据支付业务订单号查询核心交易结果，适合支付系统补偿或状态确认。")
    public CoreTransactionResponse getByBizOrderId(
            @Parameter(description = "支付系统业务订单号", example = "PAY202604040001")
            @PathVariable String bizOrderId
    ) {
        CoreTransactionResponse response = applicationService.getByBizOrderId(bizOrderId);
        if (response == null) {
            throw new ResponseStatusException(NOT_FOUND, "Transaction not found for bizOrderId=" + bizOrderId);
        }
        return response;
    }

    @GetMapping
    @Operation(summary = "按请求号查单", description = "根据幂等请求号查询核心交易结果，适合重试场景下确认请求是否已被核心受理。")
    public CoreTransactionResponse getByRequestId(
            @Parameter(description = "幂等请求号", example = "REQ-20260404-0001")
            @RequestParam String requestId
    ) {
        CoreTransactionResponse response = applicationService.getByRequestId(requestId);
        if (response == null) {
            throw new ResponseStatusException(NOT_FOUND, "Transaction not found for requestId=" + requestId);
        }
        return response;
    }
}
