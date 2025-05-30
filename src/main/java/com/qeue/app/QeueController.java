package com.qeue.app;


import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class QeueController {

    private final QeueService qeueService;

    public QeueController(QeueService qeueService) {
        this.qeueService = qeueService;
    }

    @RequestMapping("/**")
    public Mono<ResponseEntity<JsonNode>> proxyRequest(HttpServletRequest request, @RequestBody(required = false) Mono<String> bodyMono) {
        return qeueService.enqueue(request, bodyMono);
    }
}
