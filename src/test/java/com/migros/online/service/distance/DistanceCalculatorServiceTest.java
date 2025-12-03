package com.migros.online.service.distance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DistanceCalculatorService Unit Tests")
class DistanceCalculatorServiceTest {

    @Mock
    private DistanceCalculationStrategy mockStrategy;

    @Mock
    private DistanceCalculationStrategy anotherMockStrategy;

    private DistanceCalculatorService service;

    @BeforeEach
    void setUp() {
        when(mockStrategy.getStrategyName()).thenReturn("MockStrategy");
        service = new DistanceCalculatorService(mockStrategy);
    }

    @Test
    @DisplayName("Should initialize with default strategy")
    void shouldInitializeWithDefaultStrategy() {
        assertEquals(mockStrategy, service.getStrategy());
    }

    @Test
    @DisplayName("Should calculate distance using current strategy")
    void shouldCalculateDistanceUsingCurrentStrategy() {
        when(mockStrategy.calculateDistance(1.0, 2.0, 3.0, 4.0)).thenReturn(100.0);

        double result = service.calculateDistance(1.0, 2.0, 3.0, 4.0);

        assertEquals(100.0, result);
        verify(mockStrategy).calculateDistance(1.0, 2.0, 3.0, 4.0);
    }

    @Test
    @DisplayName("Should switch strategy successfully")
    void shouldSwitchStrategySuccessfully() {
        when(anotherMockStrategy.getStrategyName()).thenReturn("AnotherMockStrategy");
        
        service.setStrategy(anotherMockStrategy);

        assertEquals(anotherMockStrategy, service.getStrategy());
    }

    @Test
    @DisplayName("Should use new strategy after switching")
    void shouldUseNewStrategyAfterSwitching() {
        when(anotherMockStrategy.getStrategyName()).thenReturn("AnotherMockStrategy");
        when(anotherMockStrategy.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(200.0);

        service.setStrategy(anotherMockStrategy);
        double result = service.calculateDistance(1.0, 2.0, 3.0, 4.0);

        assertEquals(200.0, result);
        verify(anotherMockStrategy).calculateDistance(1.0, 2.0, 3.0, 4.0);
        verify(mockStrategy, never()).calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Should work with HaversineDistanceStrategy")
    void shouldWorkWithHaversineDistanceStrategy() {
        HaversineDistanceStrategy haversineStrategy = new HaversineDistanceStrategy();
        DistanceCalculatorService realService = new DistanceCalculatorService(haversineStrategy);

        double distance = realService.calculateDistance(
                41.0082, 28.9784,  // Istanbul
                39.9334, 32.8597   // Ankara
        );

        assertTrue(distance > 300000 && distance < 400000);
    }

    @Test
    @DisplayName("Should work with EuclideanDistanceStrategy")
    void shouldWorkWithEuclideanDistanceStrategy() {
        EuclideanDistanceStrategy euclideanStrategy = new EuclideanDistanceStrategy();
        DistanceCalculatorService realService = new DistanceCalculatorService(euclideanStrategy);

        double distance = realService.calculateDistance(
                40.9923307, 29.1244229,
                40.9923500, 29.1244500
        );

        assertTrue(distance < 100);
    }

    @Test
    @DisplayName("Should switch from Haversine to Euclidean strategy")
    void shouldSwitchFromHaversineToEuclidean() {
        HaversineDistanceStrategy haversineStrategy = new HaversineDistanceStrategy();
        EuclideanDistanceStrategy euclideanStrategy = new EuclideanDistanceStrategy();
        
        DistanceCalculatorService realService = new DistanceCalculatorService(haversineStrategy);
        assertEquals("Haversine", realService.getStrategy().getStrategyName());

        realService.setStrategy(euclideanStrategy);
        assertEquals("Euclidean", realService.getStrategy().getStrategyName());
    }

    @Test
    @DisplayName("Should handle zero distance")
    void shouldHandleZeroDistance() {
        when(mockStrategy.calculateDistance(1.0, 2.0, 1.0, 2.0)).thenReturn(0.0);

        double result = service.calculateDistance(1.0, 2.0, 1.0, 2.0);

        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("Should handle negative coordinates")
    void shouldHandleNegativeCoordinates() {
        when(mockStrategy.calculateDistance(-1.0, -2.0, -3.0, -4.0)).thenReturn(150.0);

        double result = service.calculateDistance(-1.0, -2.0, -3.0, -4.0);

        assertEquals(150.0, result);
        verify(mockStrategy).calculateDistance(-1.0, -2.0, -3.0, -4.0);
    }
}

