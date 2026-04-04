package com.payhub.bankcore.controller;

import com.payhub.bankcore.application.dto.CoreTransactionResponse;
import com.payhub.bankcore.application.dto.CreateCoreTransactionRequest;
import com.payhub.bankcore.application.service.CoreTransactionApplicationService;
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
public class CoreTransactionController {

    private final CoreTransactionApplicationService applicationService;

    public CoreTransactionController(CoreTransactionApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public CoreTransactionResponse create(@Valid @RequestBody CreateCoreTransactionRequest request) {
        return applicationService.create(request);
    }

    @GetMapping("/{bizOrderId}")
    public CoreTransactionResponse getByBizOrderId(@PathVariable String bizOrderId) {
        CoreTransactionResponse response = applicationService.getByBizOrderId(bizOrderId);
        if (response == null) {
            throw new ResponseStatusException(NOT_FOUND, "Transaction not found for bizOrderId=" + bizOrderId);
        }
        return response;
    }

    @GetMapping
    public CoreTransactionResponse getByRequestId(@RequestParam String requestId) {
        CoreTransactionResponse response = applicationService.getByRequestId(requestId);
        if (response == null) {
            throw new ResponseStatusException(NOT_FOUND, "Transaction not found for requestId=" + requestId);
        }
        return response;
    }
}
