package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsClient {

    @Value("${stats-server.url}")
    private static String STATS_SERVER_URL;

    private final RestClient restClient;

    public StatsClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void save(EndpointHit endpointHit) {

        String uri = UriComponentsBuilder
                .fromHttpUrl(STATS_SERVER_URL)
                .path("/hit")
                .build()
                .toUriString();

        restClient.post()
                .uri(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(endpointHit)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<ViewStats> get(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(STATS_SERVER_URL)
                .path("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris)
                .queryParam("unique", unique)
                .build()
                .toUriString();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}