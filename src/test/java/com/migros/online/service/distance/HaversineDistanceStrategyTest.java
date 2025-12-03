package com.migros.online.service.distance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HaversineDistanceStrategy Unit Tests")
class HaversineDistanceStrategyTest {

    private HaversineDistanceStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new HaversineDistanceStrategy();
    }

    @Test
    @DisplayName("Should return correct strategy name")
    void shouldReturnCorrectStrategyName() {
        assertEquals("Haversine", strategy.getStrategyName());
    }

    @Test
    @DisplayName("Should calculate distance between Istanbul and Ankara correctly")
    void shouldCalculateDistanceBetweenIstanbulAndAnkara() {
        // Istanbul to Ankara is approximately 350 km
        double distance = strategy.calculateDistance(
                41.0082, 28.9784,  // Istanbul
                39.9334, 32.8597   // Ankara
        );

        // Should be around 350 km (350000 meters)
        assertTrue(distance > 300000 && distance < 400000,
                "Distance should be approximately 350km, but was: " + distance);
    }

    @Test
    @DisplayName("Should calculate distance within 100 meters correctly")
    void shouldCalculateDistanceWithin100Meters() {
        // Two points very close together (should be < 100m)
        double distance = strategy.calculateDistance(
                40.9923307, 29.1244229,
                40.9923500, 29.1244500
        );

        assertTrue(distance < 100, "Distance should be less than 100 meters, but was: " + distance);
    }

    @Test
    @DisplayName("Should return zero when same coordinates")
    void shouldReturnZeroForSameCoordinates() {
        double distance = strategy.calculateDistance(
                40.9923307, 29.1244229,
                40.9923307, 29.1244229
        );

        assertEquals(0, distance, 0.001, "Distance should be zero for same coordinates");
    }

    @Test
    @DisplayName("Should calculate distance at equator correctly")
    void shouldCalculateDistanceAtEquator() {
        // 1 degree longitude at equator is approximately 111 km
        double distance = strategy.calculateDistance(
                0.0, 0.0,
                0.0, 1.0
        );

        assertTrue(distance > 110000 && distance < 112000,
                "1 degree longitude at equator should be ~111km, but was: " + distance);
    }

    @Test
    @DisplayName("Should calculate distance across hemisphere correctly")
    void shouldCalculateDistanceAcrossHemisphere() {
        // North pole to South pole (half of Earth's circumference ~ 20,000 km)
        double distance = strategy.calculateDistance(
                90.0, 0.0,   // North Pole
                -90.0, 0.0   // South Pole
        );

        assertTrue(distance > 19000000 && distance < 21000000,
                "Distance from North to South pole should be ~20,000km, but was: " + distance);
    }

    @Test
    @DisplayName("Should handle negative coordinates")
    void shouldHandleNegativeCoordinates() {
        // Sydney, Australia to Rio de Janeiro, Brazil
        double distance = strategy.calculateDistance(
                -33.8688, 151.2093,  // Sydney
                -22.9068, -43.1729   // Rio de Janeiro
        );

        // Should be around 13,500 km
        assertTrue(distance > 13000000 && distance < 14000000,
                "Distance Sydney to Rio should be ~13,500km, but was: " + distance);
    }

    @Test
    @DisplayName("Should calculate small distance between store and courier")
    void shouldCalculateSmallDistanceBetweenStoreAndCourier() {
        // Ataşehir MMM Migros store coordinates
        double storeLat = 40.9923307;
        double storeLng = 29.1244229;

        // Courier slightly offset (about 50 meters away)
        double courierLat = 40.9927;
        double courierLng = 29.1244;

        double distance = strategy.calculateDistance(storeLat, storeLng, courierLat, courierLng);

        assertTrue(distance > 30 && distance < 70,
                "Distance should be approximately 50 meters, but was: " + distance);
    }
}

