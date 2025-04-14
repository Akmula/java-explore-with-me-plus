package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.param.RequestParams;

import java.util.List;

@Slf4j
public class StatsClient {

    private final RestClient restClient;

    public StatsClient(String statsServerUrl) {
        this.restClient = RestClient.builder().baseUrl(statsServerUrl).build();
    }

    public void saveHit(EndpointHit endpointHit) {
        log.info("StatsClient - сохранение Hit - {}", endpointHit);
        String uri = UriComponentsBuilder
                .fromPath("/hit")
                .build()
                .toUriString();

        restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHit)
                .retrieve()
                .toBodilessEntity();
    }

    public List<ViewStats> getStats(RequestParams params) {
        log.info("StatsClient - получение статистики с параметрами: {} ", params);
        String uri = UriComponentsBuilder
                .fromPath("/stats")
                .queryParam("start", params.getStart())
                .queryParam("end", params.getEnd())
                .queryParam("uris", params.getUris())
                .queryParam("unique", params.isUnique())
                .build()
                .toUriString();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}