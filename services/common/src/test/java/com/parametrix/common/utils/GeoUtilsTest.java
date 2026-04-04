package com.parametrix.common.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeoUtilsTest {

    // Mumbai coordinates
    private static final double MUMBAI_LAT = 19.0760;
    private static final double MUMBAI_LON = 72.8777;
    
    // Delhi coordinates
    private static final double DELHI_LAT = 28.6139;
    private static final double DELHI_LON = 77.2090;
    
    // Approximate distance Mumbai to Delhi: 1,153 km

    @Test
    void calculateDistance_returnsCorrectDistanceBetweenCities() {
        double distance = GeoUtils.calculateDistance(
                MUMBAI_LAT, MUMBAI_LON, 
                DELHI_LAT, DELHI_LON
        );
        
        // Should be approximately 1,150 km (allow 5% tolerance)
        assertThat(distance).isCloseTo(1150.0, within(100.0));
    }

    @Test
    void calculateDistance_returnsZeroForSamePoint() {
        double distance = GeoUtils.calculateDistance(
                MUMBAI_LAT, MUMBAI_LON, 
                MUMBAI_LAT, MUMBAI_LON
        );
        
        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    void calculateDistance_isSymmetric() {
        double distanceAB = GeoUtils.calculateDistance(
                MUMBAI_LAT, MUMBAI_LON, 
                DELHI_LAT, DELHI_LON
        );
        
        double distanceBA = GeoUtils.calculateDistance(
                DELHI_LAT, DELHI_LON,
                MUMBAI_LAT, MUMBAI_LON
        );
        
        assertThat(distanceAB).isEqualTo(distanceBA);
    }

    @Test
    void calculateDistance_handlesAntipodalPoints() {
        // Approximately antipodal points (should be about half Earth's circumference)
        double distance = GeoUtils.calculateDistance(0, 0, 0, 180);
        
        // Half circumference is about 20,000 km
        assertThat(distance).isCloseTo(20000.0, within(100.0));
    }

    @Test
    void isWithinRadius_returnsTrueWhenWithinRadius() {
        // Point 1km away from Mumbai
        double nearbyLat = MUMBAI_LAT + 0.009; // ~1km north
        double nearbyLon = MUMBAI_LON;
        
        boolean isWithin = GeoUtils.isWithinRadius(
                MUMBAI_LAT, MUMBAI_LON,
                nearbyLat, nearbyLon,
                5.0 // 5km radius
        );
        
        assertThat(isWithin).isTrue();
    }

    @Test
    void isWithinRadius_returnsFalseWhenOutsideRadius() {
        boolean isWithin = GeoUtils.isWithinRadius(
                MUMBAI_LAT, MUMBAI_LON,
                DELHI_LAT, DELHI_LON,
                100.0 // 100km radius - Delhi is ~1150km away
        );
        
        assertThat(isWithin).isFalse();
    }

    @Test
    void isWithinRadius_returnsTrueForSamePoint() {
        boolean isWithin = GeoUtils.isWithinRadius(
                MUMBAI_LAT, MUMBAI_LON,
                MUMBAI_LAT, MUMBAI_LON,
                0.0 // Even with 0 radius
        );
        
        assertThat(isWithin).isTrue();
    }

    @Test
    void isValidLatitude_returnsTrueForValidLatitudes() {
        assertThat(GeoUtils.isValidLatitude(0)).isTrue();
        assertThat(GeoUtils.isValidLatitude(45.5)).isTrue();
        assertThat(GeoUtils.isValidLatitude(-45.5)).isTrue();
        assertThat(GeoUtils.isValidLatitude(90)).isTrue();
        assertThat(GeoUtils.isValidLatitude(-90)).isTrue();
    }

    @Test
    void isValidLatitude_returnsFalseForInvalidLatitudes() {
        assertThat(GeoUtils.isValidLatitude(90.1)).isFalse();
        assertThat(GeoUtils.isValidLatitude(-90.1)).isFalse();
        assertThat(GeoUtils.isValidLatitude(180)).isFalse();
    }

    @Test
    void isValidLongitude_returnsTrueForValidLongitudes() {
        assertThat(GeoUtils.isValidLongitude(0)).isTrue();
        assertThat(GeoUtils.isValidLongitude(90)).isTrue();
        assertThat(GeoUtils.isValidLongitude(-90)).isTrue();
        assertThat(GeoUtils.isValidLongitude(180)).isTrue();
        assertThat(GeoUtils.isValidLongitude(-180)).isTrue();
    }

    @Test
    void isValidLongitude_returnsFalseForInvalidLongitudes() {
        assertThat(GeoUtils.isValidLongitude(180.1)).isFalse();
        assertThat(GeoUtils.isValidLongitude(-180.1)).isFalse();
        assertThat(GeoUtils.isValidLongitude(360)).isFalse();
    }
}
