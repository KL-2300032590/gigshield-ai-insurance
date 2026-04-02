package com.gigshield.trigger.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
public class WeatherApiClient {
    
    private final WebClient webClient;
    private final String apiKey;
    
    public WeatherApiClient(
            @Value("${external-api.weather.base-url}") String baseUrl,
            @Value("${external-api.weather.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
    }
    
    public Mono<WeatherData> getCurrentWeather(String city) {
        if ("demo".equals(apiKey)) {
            return Mono.just(generateMockWeatherData(city));
        }
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("q", city + ",IN")
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .bodyToMono(OpenWeatherResponse.class)
                .map(this::mapToWeatherData)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> {
                    log.warn("Weather API call failed for {}: {}", city, e.getMessage());
                    return Mono.just(generateMockWeatherData(city));
                });
    }
    
    public Mono<WeatherData> getCurrentWeatherByCoords(double lat, double lon) {
        if ("demo".equals(apiKey)) {
            return Mono.just(generateMockWeatherData("Unknown"));
        }
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .bodyToMono(OpenWeatherResponse.class)
                .map(this::mapToWeatherData)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> {
                    log.warn("Weather API call failed: {}", e.getMessage());
                    return Mono.just(generateMockWeatherData("Unknown"));
                });
    }
    
    private WeatherData mapToWeatherData(OpenWeatherResponse response) {
        return WeatherData.builder()
                .city(response.getName())
                .temperature(response.getMain().getTemp())
                .humidity(response.getMain().getHumidity())
                .rainfall(response.getRain() != null ? response.getRain().getOneHour() : 0.0)
                .windSpeed(response.getWind().getSpeed())
                .description(response.getWeather() != null && !response.getWeather().isEmpty() 
                        ? response.getWeather().get(0).getDescription() : "")
                .build();
    }
    
    private WeatherData generateMockWeatherData(String city) {
        // Generate realistic mock data for testing
        double baseTemp = 28 + (Math.random() * 10);
        double rainfall = Math.random() > 0.7 ? Math.random() * 100 : 0;
        
        return WeatherData.builder()
                .city(city)
                .temperature(Math.round(baseTemp * 10.0) / 10.0)
                .humidity((int) (50 + Math.random() * 40))
                .rainfall(Math.round(rainfall * 10.0) / 10.0)
                .windSpeed(Math.round(Math.random() * 20 * 10.0) / 10.0)
                .description(rainfall > 50 ? "Heavy Rain" : rainfall > 0 ? "Light Rain" : "Clear")
                .build();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherData {
        private String city;
        private double temperature;
        private int humidity;
        private double rainfall;  // mm per hour
        private double windSpeed;
        private String description;
    }
    
    @Data
    private static class OpenWeatherResponse {
        private String name;
        private Main main;
        private Wind wind;
        private Rain rain;
        private java.util.List<Weather> weather;
        
        @Data
        static class Main {
            private double temp;
            private int humidity;
        }
        
        @Data
        static class Wind {
            private double speed;
        }
        
        @Data
        static class Rain {
            private double oneHour;
        }
        
        @Data
        static class Weather {
            private String description;
        }
    }
}
