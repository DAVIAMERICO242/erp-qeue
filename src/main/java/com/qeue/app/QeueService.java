package com.qeue.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class QeueService {
    private final BlockingQueue<QeuedRequest> queue = new LinkedBlockingQueue<>();
    private final WebClient webClient = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                    .build())
            .build();;
    private final String TARGET_BASE = "https://integrador.varejonline.com.br";
    private final ObjectMapper mapper = new ObjectMapper();
    private static final int delay = 300;


    public Mono<ResponseEntity<JsonNode>> enqueue(HttpServletRequest req, Mono<String> bodyMono) {
        if (bodyMono == null) {
            bodyMono = Mono.just("");
        }
        return bodyMono.defaultIfEmpty("")
                .flatMap(body -> {
                    QeuedRequest qr = new QeuedRequest(req, body);
                    queue.offer(qr);
                    return Mono.fromFuture(qr.getFuture());
                });
    }

    @PostConstruct
    public void startQueueProcessing() {
        Flux.interval(Duration.ofMillis(delay))
                .flatMap(tick -> {
                    QeuedRequest qr = queue.poll();
                    if (qr == null) return Mono.empty();

                    HttpServletRequest req = qr.getRequest();
                    String path = req.getRequestURI(); // já é a path sem host
                    String query = req.getQueryString();
                    String fullPath = path + (query != null ? "?" + query : "");

                    WebClient.RequestBodySpec requestSpec = webClient
                            .method(HttpMethod.valueOf(req.getMethod()))
                            .uri(TARGET_BASE + fullPath);

                    // Copiando os headers
                    Enumeration<String> headerNames = req.getHeaderNames();
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        if (name.equalsIgnoreCase("host")) continue;

                        Enumeration<String> values = req.getHeaders(name);
                        List<String> valueList = Collections.list(values);
                        requestSpec = requestSpec.header(name, valueList.toArray(new String[0]));
                    }

                    return requestSpec
                            .bodyValue(qr.getBody())
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .map(ResponseEntity::ok)
                            .onErrorResume(e -> {
                                ObjectNode errorJson = mapper.createObjectNode();
                                errorJson.put("error", "Erro no proxy: " + e.getMessage());
                                return Mono.just(ResponseEntity.status(500).body(errorJson));
                            })
                            .doOnNext(resp -> qr.getFuture().complete(resp));

                })
                .subscribe();
    }
}
