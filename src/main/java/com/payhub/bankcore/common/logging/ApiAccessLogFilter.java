package com.payhub.bankcore.common.logging;

import com.payhub.bankcore.common.JacksonMapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ApiAccessLogFilter extends OncePerRequestFilter {
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return false;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            String requestBody = cachedBody(wrappedRequest.getContentAsByteArray());
            String responseBody = cachedBody(wrappedResponse.getContentAsByteArray());
            requestLog(request, requestBody);
            responseLog(request, wrappedResponse, responseBody, durationMs);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void requestLog(HttpServletRequest request, String requestBody) {
        Map<String, Object> logEvent = new LinkedHashMap<>();
        logEvent.put("logType", "api.request");
        logEvent.put("method", request.getMethod());
        logEvent.put("uri", request.getRequestURI());
        logEvent.put("query", request.getQueryString());
        logEvent.put("clientIp", request.getRemoteAddr());
        logEvent.put("contentType", request.getContentType());
        logEvent.put("requestBody", JacksonMapper.toMap(requestBody));
        String reqLog = JacksonMapper.toJson(logEvent);
        log.info("::REQ::{}", reqLog);

    }

    private void responseLog(HttpServletRequest request,
                             HttpServletResponse response,
                             String responseBody,
                             long durationMs) {
        Map<String, Object> logEvent = new LinkedHashMap<>();
        logEvent.put("logType", "api.response");
        logEvent.put("method", request.getMethod());
        logEvent.put("uri", request.getRequestURI());
        logEvent.put("status", response.getStatus());
        logEvent.put("durationMs", durationMs);
        logEvent.put("responseBody", JacksonMapper.toMap(responseBody));
        String respLog = JacksonMapper.toJson(logEvent);
        ApiAccessLogFilter.log.info("::RESP::{}", respLog);
    }

    private String cachedBody(byte[] bodyBytes) {
        if (bodyBytes == null || bodyBytes.length == 0) {
            return "";
        }
        return new String(bodyBytes, StandardCharsets.UTF_8);
    }
}
