package ru.practicum;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.param.RequestParams;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class StatsClient {

    private final RestClient restClient;
    private final String appName;

    public StatsClient(String statsServerUrl, String appName) {
        this.restClient = RestClient.builder()
                .baseUrl(statsServerUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.appName = appName;
    }

    public void saveHit(HttpServletRequest request) {
        log.info("StatsClient - сохранение HttpServletRequest - {}", request);

        EndpointHit hit = new EndpointHit();
        hit.setUri(request.getRequestURI());
        hit.setApp(appName);
        hit.setIp(request.getRemoteAddr());
        hit.setTimestamp(LocalDateTime.now());

        String uri = UriComponentsBuilder
                .fromPath("/hit")
                .build()
                .toUriString();

        restClient.post()
                .uri(uri)
                .body(hit)
                .retrieve()
                .toEntity(EndpointHit.class);
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