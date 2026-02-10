package ru.practicum.stats.server.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StatsServerIntegrationTest {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void shouldSaveHitAndReturnStatsForUrisAndUniqueFlag() throws Exception {
        String baseUrl = "http://localhost:" + port;

        postHit(baseUrl, "/events/1", "1.1.1.1");
        postHit(baseUrl, "/events/1", "2.2.2.2");
        postHit(baseUrl, "/events/1", "1.1.1.1");

        String start = LocalDateTime.now().minusDays(1).format(DTF);
        String end = LocalDateTime.now().plusDays(1).format(DTF);

        URI urlAll = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", "/events/1")
                .queryParam("unique", false)
                .build()
                .encode()
                .toUri();
        ResponseEntity<String> allResp = restTemplate.getForEntity(urlAll, String.class);
        assertThat(allResp.getStatusCode().value(), is(200));
        JsonNode all = objectMapper.readTree(allResp.getBody());
        assertThat(all.isArray(), is(true));
        assertThat(all.size(), is(1));
        assertThat(all.get(0).get("uri").asText(), is("/events/1"));
        assertThat(all.get(0).get("hits").asLong(), is(3L));

        URI urlUnique = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", "/events/1")
                .queryParam("unique", true)
                .build()
                .encode()
                .toUri();
        ResponseEntity<String> uniqResp = restTemplate.getForEntity(urlUnique, String.class);
        assertThat(uniqResp.getStatusCode().value(), is(200));
        JsonNode uniq = objectMapper.readTree(uniqResp.getBody());
        assertThat(uniq.isArray(), is(true));
        assertThat(uniq.size(), is(1));
        assertThat(uniq.get(0).get("hits").asLong(), is(2L));
    }

    private void postHit(String baseUrl, String uri, String ip) {
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now().format(DTF))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/hit",
                new HttpEntity<>(dto, headers),
                String.class
        );

        assertThat(response.getStatusCode().value(), is(201));
    }
}
