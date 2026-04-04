package com.parametrix.trigger.service;

import com.parametrix.common.events.EnvironmentDisruptionEvent;
import com.parametrix.common.model.Claim.TriggerType;
import com.parametrix.trigger.client.AqiApiClient;
import com.parametrix.trigger.client.WeatherApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvironmentMonitoringServiceTest {

    @Mock
    private WeatherApiClient weatherApiClient;

    @Mock
    private AqiApiClient aqiApiClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private EnvironmentMonitoringService monitoringService;

    @BeforeEach
    void setUp() {
        // Set threshold values via reflection (simulating @Value injection)
        ReflectionTestUtils.setField(monitoringService, "rainfallThreshold", 50.0);
        ReflectionTestUtils.setField(monitoringService, "aqiThreshold", 300);
        ReflectionTestUtils.setField(monitoringService, "highTempThreshold", 42.0);
        ReflectionTestUtils.setField(monitoringService, "lowTempThreshold", 5.0);
        ReflectionTestUtils.setField(monitoringService, "monitoredCities", 
                List.of("Mumbai", "Delhi", "Bangalore"));
    }

    @Test
    void monitorCity_triggersHeavyRainEventWhenRainfallExceedsThreshold() {
        WeatherApiClient.WeatherData heavyRainWeather = WeatherApiClient.WeatherData.builder()
                .temperature(25.0)
                .humidity(90)
                .rainfall(75.0) // Above 50mm threshold
                .windSpeed(15.0)
                .build();

        AqiApiClient.AqiData normalAqi = AqiApiClient.AqiData.builder()
                .aqi(100)
                .build();

        when(weatherApiClient.getCurrentWeather("Mumbai")).thenReturn(Mono.just(heavyRainWeather));
        when(aqiApiClient.getCurrentAqi("Mumbai")).thenReturn(Mono.just(normalAqi));

        monitoringService.monitorCity("Mumbai");

        // Wait for async processing
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        ArgumentCaptor<EnvironmentDisruptionEvent> eventCaptor = 
                ArgumentCaptor.forClass(EnvironmentDisruptionEvent.class);
        verify(kafkaTemplate, atLeastOnce()).send(anyString(), anyString(), eventCaptor.capture());

        EnvironmentDisruptionEvent event = eventCaptor.getValue();
        assertThat(event.getTriggerType()).isEqualTo(TriggerType.HEAVY_RAIN);
        assertThat(event.getCity()).isEqualTo("Mumbai");
        assertThat(event.getMeasuredValue()).isEqualTo(75.0);
        assertThat(event.getThreshold()).isEqualTo(50.0);
    }

    @Test
    void monitorCity_triggersExtremeHeatEventWhenTemperatureExceedsThreshold() {
        WeatherApiClient.WeatherData extremeHeatWeather = WeatherApiClient.WeatherData.builder()
                .temperature(45.0) // Above 42°C threshold
                .humidity(30)
                .rainfall(0)
                .windSpeed(5.0)
                .build();

        AqiApiClient.AqiData normalAqi = AqiApiClient.AqiData.builder()
                .aqi(100)
                .build();

        when(weatherApiClient.getCurrentWeather("Delhi")).thenReturn(Mono.just(extremeHeatWeather));
        when(aqiApiClient.getCurrentAqi("Delhi")).thenReturn(Mono.just(normalAqi));

        monitoringService.monitorCity("Delhi");

        // Wait for async processing
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        ArgumentCaptor<EnvironmentDisruptionEvent> eventCaptor = 
                ArgumentCaptor.forClass(EnvironmentDisruptionEvent.class);
        verify(kafkaTemplate, atLeastOnce()).send(anyString(), anyString(), eventCaptor.capture());

        EnvironmentDisruptionEvent event = eventCaptor.getValue();
        assertThat(event.getTriggerType()).isEqualTo(TriggerType.EXTREME_HEAT);
        assertThat(event.getMeasuredValue()).isEqualTo(45.0);
    }

    @Test
    void monitorCity_triggersExtremeColdEventWhenTemperatureBelowThreshold() {
        WeatherApiClient.WeatherData coldWeather = WeatherApiClient.WeatherData.builder()
                .temperature(2.0) // Below 5°C threshold
                .humidity(70)
                .rainfall(0)
                .windSpeed(10.0)
                .build();

        AqiApiClient.AqiData normalAqi = AqiApiClient.AqiData.builder()
                .aqi(50)
                .build();

        when(weatherApiClient.getCurrentWeather("Delhi")).thenReturn(Mono.just(coldWeather));
        when(aqiApiClient.getCurrentAqi("Delhi")).thenReturn(Mono.just(normalAqi));

        monitoringService.monitorCity("Delhi");

        // Wait for async processing
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        ArgumentCaptor<EnvironmentDisruptionEvent> eventCaptor = 
                ArgumentCaptor.forClass(EnvironmentDisruptionEvent.class);
        verify(kafkaTemplate, atLeastOnce()).send(anyString(), anyString(), eventCaptor.capture());

        EnvironmentDisruptionEvent event = eventCaptor.getValue();
        assertThat(event.getTriggerType()).isEqualTo(TriggerType.EXTREME_COLD);
    }

    @Test
    void monitorCity_triggersAirPollutionEventWhenAqiExceedsThreshold() {
        WeatherApiClient.WeatherData normalWeather = WeatherApiClient.WeatherData.builder()
                .temperature(30.0)
                .humidity(60)
                .rainfall(0)
                .windSpeed(5.0)
                .build();

        AqiApiClient.AqiData hazardousAqi = AqiApiClient.AqiData.builder()
                .aqi(350) // Above 300 threshold
                .build();

        when(weatherApiClient.getCurrentWeather("Delhi")).thenReturn(Mono.just(normalWeather));
        when(aqiApiClient.getCurrentAqi("Delhi")).thenReturn(Mono.just(hazardousAqi));

        monitoringService.monitorCity("Delhi");

        // Wait for async processing
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        ArgumentCaptor<EnvironmentDisruptionEvent> eventCaptor = 
                ArgumentCaptor.forClass(EnvironmentDisruptionEvent.class);
        verify(kafkaTemplate, atLeastOnce()).send(anyString(), anyString(), eventCaptor.capture());

        EnvironmentDisruptionEvent event = eventCaptor.getValue();
        assertThat(event.getTriggerType()).isEqualTo(TriggerType.AIR_POLLUTION);
        assertThat(event.getMeasuredValue()).isEqualTo(350.0);
    }

    @Test
    void monitorCity_doesNotTriggerEventWhenConditionsAreNormal() {
        WeatherApiClient.WeatherData normalWeather = WeatherApiClient.WeatherData.builder()
                .temperature(25.0) // Normal temperature
                .humidity(60)
                .rainfall(10.0) // Below threshold
                .windSpeed(10.0)
                .build();

        AqiApiClient.AqiData normalAqi = AqiApiClient.AqiData.builder()
                .aqi(100) // Below threshold
                .build();

        when(weatherApiClient.getCurrentWeather("Bangalore")).thenReturn(Mono.just(normalWeather));
        when(aqiApiClient.getCurrentAqi("Bangalore")).thenReturn(Mono.just(normalAqi));

        monitoringService.monitorCity("Bangalore");

        // Wait for async processing
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void calculateSeverity_returnsCriticalWhenValueIsDouble() {
        // Test via reflection - severity should be CRITICAL when value >= 2x threshold
        WeatherApiClient.WeatherData extremeWeather = WeatherApiClient.WeatherData.builder()
                .temperature(25.0)
                .humidity(80)
                .rainfall(110.0) // 2.2x threshold (50mm)
                .windSpeed(20.0)
                .build();

        AqiApiClient.AqiData normalAqi = AqiApiClient.AqiData.builder()
                .aqi(50)
                .build();

        when(weatherApiClient.getCurrentWeather("Mumbai")).thenReturn(Mono.just(extremeWeather));
        when(aqiApiClient.getCurrentAqi("Mumbai")).thenReturn(Mono.just(normalAqi));

        monitoringService.monitorCity("Mumbai");

        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        ArgumentCaptor<EnvironmentDisruptionEvent> eventCaptor = 
                ArgumentCaptor.forClass(EnvironmentDisruptionEvent.class);
        verify(kafkaTemplate, atLeastOnce()).send(anyString(), anyString(), eventCaptor.capture());

        EnvironmentDisruptionEvent event = eventCaptor.getValue();
        assertThat(event.getSeverity()).isEqualTo(EnvironmentDisruptionEvent.Severity.CRITICAL);
    }
}
