package com.migros.online.service.distance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EuclideanDistanceStrategy Unit Tests")
class EuclideanDistanceStrategyTest {

    private EuclideanDistanceStrategy strategy;
    private HaversineDistanceStrategy haversineStrategy;

    @BeforeEach
    void setUp() {
        strategy = new EuclideanDistanceStrategy();
        haversineStrategy = new HaversineDistanceStrategy();
    }

    @Test
    @DisplayName("Should return correct strategy name")
    void shouldReturnCorrectStrategyName() {
        assertEquals("Euclidean", strategy.getStrategyName());
    }

    @Test
    @DisplayName("Should calculate distance between Istanbul and Ankara correctly")
    void shouldCalculateDistanceBetweenIstanbulAndAnkara() {
        // Istanbul to Ankara
        double distance = strategy.calculateDistance(
                41.0082, 28.9784,  // Istanbul
                39.9334, 32.8597   // Ankara
        );

        // Euclidean should give a similar result to Haversine for moderate distances
        assertTrue(distance > 300000 && distance < 450000,
                "Distance should be approximately 350km, but was: " + distance);
    }

    @Test
    @DisplayName("Should be within 10% of Haversine for small distances")
    void shouldBeWithin10PercentOfHaversineForSmallDistances() {
        // For small distances, Euclidean should be very close to Haversine
        double euclideanDist = strategy.calculateDistance(
                40.9923307, 29.1244229,
                40.9923500, 29.1244500
        );
        double haversineDist = haversineStrategy.calculateDistance(
                40.9923307, 29.1244229,
                40.9923500, 29.1244500
        );

        // Both should be within 10% of each other for small distances
        double difference = Math.abs(euclideanDist - haversineDist);
        double percentDiff = difference / haversineDist * 100;
        assertTrue(percentDiff < 10, 
                "Euclidean and Haversine should be within 10% for small distances, but difference was: " + percentDiff + "%");
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
    @DisplayName("Should calculate distance at different latitudes")
    void shouldCalculateDistanceAtDifferentLatitudes() {
        // 1 degree latitude is approximately 111 km everywhere
        double distance = strategy.calculateDistance(
                40.0, 29.0,
                41.0, 29.0
        );

        assertTrue(distance > 110000 && distance < 113000,
                "1 degree latitude should be ~111km, but was: " + distance);
    }

    @Test
    @DisplayName("Should handle negative coordinates")
    void shouldHandleNegativeCoordinates() {
        double distance = strategy.calculateDistance(
                -10.0, -10.0,
                -11.0, -11.0
        );

        assertTrue(distance > 0, "Distance should be positive");
    }

    @Test
    @DisplayName("Should calculate small distance accurately for store proximity check")
    void shouldCalculateSmallDistanceAccuratelyForStoreProximityCheck() {
        // Ataşehir MMM Migros store coordinates
        double storeLat = 40.9923307;
        double storeLng = 29.1244229;

        // Courier at exact store location
        double distance = strategy.calculateDistance(storeLat, storeLng, storeLat, storeLng);
        assertEquals(0, distance, 0.001);

        // Courier very close (within 100m radius)
        double closeDistance = strategy.calculateDistance(
                storeLat, storeLng,
                40.9924, 29.1244
        );
        assertTrue(closeDistance < 100, "Distance should be within 100m, but was: " + closeDistance);
    }

    @Test
    @DisplayName("Should calculate medium distance between two stores")
    void shouldCalculateMediumDistanceBetweenTwoStores() {
        // Distance between two Migros stores in Istanbul
        double distance = strategy.calculateDistance(
                40.9923307, 29.1244229,  // Ataşehir MMM Migros
                41.0551273, 28.9343891   // Beylikdüzü 5M Migros
        );

        // Should be several kilometers
        assertTrue(distance > 10000 && distance < 30000,
                "Distance between stores should be 10-30km, but was: " + distance);
    }
}

