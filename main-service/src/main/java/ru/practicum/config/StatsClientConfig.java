package ru.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.StatsClient;

@Configuration
public class StatsClientConfig {

    @Bean
    public StatsClient statsClient(@Value("${stats-server.url}") String statsServerUrl,
                                   @Value("${application.name}") String appName) {
        return new StatsClient(statsServerUrl, appName);
    }
}