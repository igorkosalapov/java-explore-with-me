package ru.practicum.stats.client;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class StatsClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public StatsClient(String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public void hit(EndpointHitDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EndpointHitDto> request = new HttpEntity<>(dto, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/hit",
                HttpMethod.POST,
                request,
                Void.class
        );

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new IllegalStateException("Unexpected status from stats /hit: " + response.getStatusCode());
        }
    }

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {

        String encodedStart = URLEncoder.encode(start, StandardCharsets.UTF_8);
        String encodedEnd = URLEncoder.encode(end, StandardCharsets.UTF_8);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/stats")
                .queryParam("start", encodedStart)
                .queryParam("end", encodedEnd)
                .queryParam("unique", unique);

        if (uris != null) {
            for (String uri : uris) {
                builder.queryParam("uris", uri);
            }
        }

        URI uri = builder.build(true).toUri();

        ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(uri, ViewStatsDto[].class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new IllegalStateException("Unexpected response from stats /stats: " + response.getStatusCode());
        }

        return Arrays.asList(response.getBody());
    }
}
