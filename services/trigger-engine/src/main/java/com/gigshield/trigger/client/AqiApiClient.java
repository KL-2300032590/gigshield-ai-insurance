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
public class AqiApiClient {
    
    private final WebClient webClient;
    private final String apiKey;
    
    public AqiApiClient(
            @Value("${external-api.aqi.base-url}") String baseUrl,
            @Value("${external-api.aqi.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
    }
    
    public Mono<AqiData> getCurrentAqi(String city) {
        if ("demo".equals(apiKey)) {
            return Mono.just(generateMockAqiData(city));
        }
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/feed/{city}/")
                        .queryParam("token", apiKey)
                        .build(city))
                .retrieve()
                .bodyToMono(WaqiResponse.class)
                .map(this::mapToAqiData)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> {
                    log.warn("AQI API call failed for {}: {}", city, e.getMessage());
                    return Mono.just(generateMockAqiData(city));
                });
    }
    
    public Mono<AqiData> getCurrentAqiByCoords(double lat, double lon) {
        if ("demo".equals(apiKey)) {
            return Mono.just(generateMockAqiData("Unknown"));
        }
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/feed/geo:{lat};{lon}/")
                        .queryParam("token", apiKey)
                        .build(lat, lon))
                .retrieve()
                .bodyToMono(WaqiResponse.class)
                .map(this::mapToAqiData)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> {
                    log.warn("AQI API call failed: {}", e.getMessage());
                    return Mono.just(generateMockAqiData("Unknown"));
                });
    }
    
    private AqiData mapToAqiData(WaqiResponse response) {
        if (response.getData() == null) {
            return generateMockAqiData("Unknown");
        }
        
        return AqiData.builder()
                .city(response.getData().getCity() != null ? response.getData().getCity().getName() : "Unknown")
                .aqi(response.getData().getAqi())
                .category(getAqiCategory(response.getData().getAqi()))
                .dominantPollutant(response.getData().getDominentpol())
                .build();
    }
    
    private AqiData generateMockAqiData(String city) {
        // Generate realistic mock data based on city
        int baseAqi = 100;
        
        if (city != null) {
            String cityLower = city.toLowerCase();
            if (cityLower.contains("delhi")) {
                baseAqi = 200 + (int) (Math.random() * 150);
            } else if (cityLower.contains("mumbai") || cityLower.contains("kolkata")) {
                baseAqi = 100 + (int) (Math.random() * 100);
            } else {
                baseAqi = 50 + (int) (Math.random() * 100);
            }
        }
        
        return AqiData.builder()
                .city(city)
                .aqi(baseAqi)
                .category(getAqiCategory(baseAqi))
                .dominantPollutant("pm25")
                .build();
    }
    
    private String getAqiCategory(int aqi) {
        if (aqi <= 50) return "Good";
        if (aqi <= 100) return "Moderate";
        if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        if (aqi <= 200) return "Unhealthy";
        if (aqi <= 300) return "Very Unhealthy";
        return "Hazardous";
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AqiData {
        private String city;
        private int aqi;
        private String category;
        private String dominantPollutant;
    }
    
    @Data
    private static class WaqiResponse {
        private String status;
        private WaqiData data;
        
        @Data
        static class WaqiData {
            private int aqi;
            private String dominentpol;
            private City city;
            
            @Data
            static class City {
                private String name;
            }
        }
    }
}
