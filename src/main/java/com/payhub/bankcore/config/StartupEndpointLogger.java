package com.payhub.bankcore.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupEndpointLogger implements ApplicationListener<WebServerInitializedEvent> {

    private final Environment environment;

    public StartupEndpointLogger(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        int port = event.getWebServer().getPort();
        String localUrl = "http://localhost:" + port + contextPath;
        String externalUrl = "http://" + getHostAddress() + ":" + port + contextPath;

        log.info("bank-core started");
        log.info("Backend Local URL: {}", localUrl);
        log.info("Backend External URL: {}", externalUrl);
        log.info("Health Check: {}/internal/health", localUrl);
        log.info("Swagger UI: {}/swagger-ui/index.html", localUrl);
        log.info("Admin Test Page: {}/admin-test.html", localUrl);
    }

    private String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            return "127.0.0.1";
        }
    }
}
