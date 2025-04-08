package ru.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.StatsClient;

@Configuration
public class StatsConfig {

    @Bean
    public StatsClient getClient(@Value("${stats-server-url}") String statsServerUrl) {
        return new StatsClient(statsServerUrl);
    }
}