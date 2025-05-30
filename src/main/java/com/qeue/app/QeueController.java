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

    private String secret =  "23HJ9DF9238JRJ893289R4-54G3484TR43287R423R732R423D743214D1QWD";

    private final QeueService qeueService;

    public QeueController(QeueService qeueService) {
        this.qeueService = qeueService;
    }

    @RequestMapping("/**")
    public Mono<ResponseEntity<JsonNode>> proxyRequest(HttpServletRequest request, @RequestBody(required = false) Mono<String> bodyMono) {
        String providedSecret = request.getHeader("secret");
        if (providedSecret == null || !providedSecret.equals(secret)) {
            return Mono.just(ResponseEntity.status(403).body(null)); // 403 Forbidden
        }
        return qeueService.enqueue(request, bodyMono);
    }
}
