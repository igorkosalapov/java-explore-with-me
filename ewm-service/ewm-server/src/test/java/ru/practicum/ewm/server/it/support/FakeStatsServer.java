package ru.practicum.ewm.server.it.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class FakeStatsServer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Long> hitsByUri = new ConcurrentHashMap<>();
    private HttpServer server;
    private String baseUrl;

    public void start() throws IOException {
        if (server != null) {
            return;
        }
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/hit", this::handleHit);
        server.createContext("/stats", this::handleStats);

        server.start();
        int port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public String getBaseUrl() {
        return Objects.requireNonNull(baseUrl, "FakeStatsServer is not started");
    }

    public Map<String, Long> getHitsByUri() {
        return Collections.unmodifiableMap(hitsByUri);
    }

    public void reset() {
        hitsByUri.clear();
    }

    private void handleHit(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        EndpointHitDto dto;
        try (InputStream is = exchange.getRequestBody()) {
            dto = objectMapper.readValue(is, EndpointHitDto.class);
        }

        if (dto != null && dto.getUri() != null) {
            hitsByUri.merge(dto.getUri(), 1L, Long::sum);
        }

        byte[] body = objectMapper.writeValueAsBytes(dto);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(201, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private void handleStats(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        URI uri = exchange.getRequestURI();
        Map<String, List<String>> params = parseQueryParams(uri.getRawQuery());

        List<String> requestedUris = params.getOrDefault("uris", List.of());
        if (requestedUris.isEmpty()) {
            requestedUris = new ArrayList<>(hitsByUri.keySet());
        }

        String app = params.getOrDefault("app", List.of("ewm"))
                .stream()
                .findFirst()
                .orElse("ewm");

        List<ViewStatsDto> response = requestedUris.stream()
                .distinct()
                .map(u -> new ViewStatsDto(app, u, hitsByUri.getOrDefault(u, 0L)))
                .collect(Collectors.toList());

        byte[] body = objectMapper.writeValueAsBytes(response);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private static Map<String, List<String>> parseQueryParams(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Map.of();
        }
        return List.of(rawQuery.split("&")).stream()
                .map(kv -> kv.split("=", 2))
                .filter(arr -> arr.length >= 1)
                .collect(Collectors.groupingBy(
                        arr -> decode(arr[0]),
                        Collectors.mapping(arr -> arr.length == 2 ? decode(arr[1]) : "", Collectors.toList())
                ));
    }

    private static String decode(String s) {
        if (s == null) {
            return "";
        }
        return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
