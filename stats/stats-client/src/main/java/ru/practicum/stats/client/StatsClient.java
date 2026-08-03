package ru.practicum.stats.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class StatsClient {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(String serverUrl) {
        this.restTemplate = new RestTemplate();
        this.serverUrl = serverUrl;
    }

    public void addHit(EndpointHitDto endpointHitDto) {
        restTemplate.postForEntity(
                serverUrl + "/hit",
                endpointHitDto,
                Void.class
        );
    }

    public List<ViewStatsDto> viewStats(LocalDateTime start,
                                        LocalDateTime end,
                                        List<String> uris,
                                        boolean unique) {

        StringBuilder requestUrl = new StringBuilder(serverUrl)
                .append("/stats")
                .append("?start=")
                .append(encode(start.format(FORMAT)))
                .append("&end=")
                .append(encode(end.format(FORMAT)));

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                requestUrl.append("&uris=")
                        .append(encode(uri));
            }
        }

        requestUrl.append("&unique=")
                .append(unique);

        ResponseEntity<ViewStatsDto[]> response =
                restTemplate.getForEntity(
                        URI.create(requestUrl.toString()),
                        ViewStatsDto[].class
                );

        ViewStatsDto[] body = response.getBody();

        return body == null
                ? List.of()
                : Arrays.asList(body);
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}
