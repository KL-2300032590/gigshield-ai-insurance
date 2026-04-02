package com.gigshield.trigger.service;

import com.gigshield.common.events.EnvironmentDisruptionEvent;
import com.gigshield.common.events.KafkaTopics;
import com.gigshield.common.model.Claim.TriggerType;
import com.gigshield.trigger.client.AqiApiClient;
import com.gigshield.trigger.client.WeatherApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentMonitoringService {
    
    private final WeatherApiClient weatherApiClient;
    private final AqiApiClient aqiApiClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${trigger.thresholds.rainfall-mm:50.0}")
    private double rainfallThreshold;
    
    @Value("${trigger.thresholds.aqi-hazardous:300}")
    private int aqiThreshold;
    
    @Value("${trigger.thresholds.temperature-high-celsius:42}")
    private double highTempThreshold;
    
    @Value("${trigger.thresholds.temperature-low-celsius:5}")
    private double lowTempThreshold;
    
    @Value("${trigger.monitoring.cities:Mumbai,Delhi,Bangalore,Chennai,Kolkata,Hyderabad}")
    private List<String> monitoredCities;
    
    @Scheduled(fixedRateString = "${trigger.monitoring.interval-seconds:300}000")
    public void monitorEnvironment() {
        log.info("Starting environmental monitoring for {} cities", monitoredCities.size());
        
        for (String city : monitoredCities) {
            monitorCity(city);
        }
    }
    
    public void monitorCity(String city) {
        // Check weather conditions
        weatherApiClient.getCurrentWeather(city)
                .subscribe(weather -> {
                    checkRainfallTrigger(city, weather);
                    checkTemperatureTrigger(city, weather);
                });
        
        // Check AQI
        aqiApiClient.getCurrentAqi(city)
                .subscribe(aqi -> checkAqiTrigger(city, aqi));
    }
    
    public void checkLocationTriggers(double latitude, double longitude, String city) {
        weatherApiClient.getCurrentWeatherByCoords(latitude, longitude)
                .subscribe(weather -> {
                    checkRainfallTrigger(city, weather);
                    checkTemperatureTrigger(city, weather);
                });
        
        aqiApiClient.getCurrentAqiByCoords(latitude, longitude)
                .subscribe(aqi -> checkAqiTrigger(city, aqi));
    }
    
    private void checkRainfallTrigger(String city, WeatherApiClient.WeatherData weather) {
        if (weather.getRainfall() >= rainfallThreshold) {
            log.warn("TRIGGER: Heavy rainfall detected in {} - {}mm (threshold: {}mm)", 
                    city, weather.getRainfall(), rainfallThreshold);
            
            publishDisruptionEvent(
                    TriggerType.HEAVY_RAIN,
                    city,
                    0, 0,
                    weather.getRainfall(),
                    rainfallThreshold,
                    "OpenWeatherMap"
            );
        }
    }
    
    private void checkTemperatureTrigger(String city, WeatherApiClient.WeatherData weather) {
        if (weather.getTemperature() >= highTempThreshold) {
            log.warn("TRIGGER: Extreme heat detected in {} - {}°C (threshold: {}°C)", 
                    city, weather.getTemperature(), highTempThreshold);
            
            publishDisruptionEvent(
                    TriggerType.EXTREME_HEAT,
                    city,
                    0, 0,
                    weather.getTemperature(),
                    highTempThreshold,
                    "OpenWeatherMap"
            );
        } else if (weather.getTemperature() <= lowTempThreshold) {
            log.warn("TRIGGER: Extreme cold detected in {} - {}°C (threshold: {}°C)", 
                    city, weather.getTemperature(), lowTempThreshold);
            
            publishDisruptionEvent(
                    TriggerType.EXTREME_COLD,
                    city,
                    0, 0,
                    weather.getTemperature(),
                    lowTempThreshold,
                    "OpenWeatherMap"
            );
        }
    }
    
    private void checkAqiTrigger(String city, AqiApiClient.AqiData aqi) {
        if (aqi.getAqi() >= aqiThreshold) {
            log.warn("TRIGGER: Hazardous AQI detected in {} - {} (threshold: {})", 
                    city, aqi.getAqi(), aqiThreshold);
            
            publishDisruptionEvent(
                    TriggerType.AIR_POLLUTION,
                    city,
                    0, 0,
                    aqi.getAqi(),
                    aqiThreshold,
                    "WAQI"
            );
        }
    }
    
    private void publishDisruptionEvent(TriggerType type, String city, 
                                         double lat, double lon,
                                         double value, double threshold, String source) {
        EnvironmentDisruptionEvent event = EnvironmentDisruptionEvent.builder()
                .eventType(EnvironmentDisruptionEvent.TYPE)
                .triggerType(type)
                .city(city)
                .latitude(lat)
                .longitude(lon)
                .measuredValue(value)
                .threshold(threshold)
                .source(source)
                .severity(calculateSeverity(value, threshold))
                .build();
        
        kafkaTemplate.send(KafkaTopics.ENVIRONMENT_DISRUPTION, city, event);
        log.info("Published EnvironmentDisruptionEvent: {} in {}", type, city);
    }
    
    private EnvironmentDisruptionEvent.Severity calculateSeverity(double value, double threshold) {
        double ratio = value / threshold;
        if (ratio >= 2.0) return EnvironmentDisruptionEvent.Severity.CRITICAL;
        if (ratio >= 1.5) return EnvironmentDisruptionEvent.Severity.HIGH;
        if (ratio >= 1.2) return EnvironmentDisruptionEvent.Severity.MEDIUM;
        return EnvironmentDisruptionEvent.Severity.LOW;
    }
}
