package com.qeue.app;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;

import java.util.concurrent.CompletableFuture;

public class QeuedRequest {
    private final HttpServletRequest request;
    private final String body;
    private final CompletableFuture<ResponseEntity<JsonNode>> future;

    public QeuedRequest(HttpServletRequest request, String body) {
        this.request = request;
        this.body = body;
        this.future = new CompletableFuture<>();
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public String getBody() {
        return body;
    }

    public CompletableFuture<ResponseEntity<JsonNode>> getFuture() {
        return future;
    }
}
