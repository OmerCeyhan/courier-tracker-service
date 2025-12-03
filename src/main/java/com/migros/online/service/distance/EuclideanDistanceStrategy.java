package com.migros.online.service.distance;

import org.springframework.stereotype.Component;

@Component
public class EuclideanDistanceStrategy implements DistanceCalculationStrategy {

    private static final double METERS_PER_DEGREE_LAT = 111320.0;

    @Override
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double avgLat = Math.toRadians((lat1 + lat2) / 2);
        
        double metersPerDegreeLng = METERS_PER_DEGREE_LAT * Math.cos(avgLat);
        
        double deltaLatMeters = (lat2 - lat1) * METERS_PER_DEGREE_LAT;
        double deltaLngMeters = (lng2 - lng1) * metersPerDegreeLng;
        
        return Math.sqrt(deltaLatMeters * deltaLatMeters + deltaLngMeters * deltaLngMeters);
    }

    @Override
    public String getStrategyName() {
        return "Euclidean";
    }
}
