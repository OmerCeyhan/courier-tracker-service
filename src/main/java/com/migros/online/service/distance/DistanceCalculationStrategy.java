package com.migros.online.service.distance;

public interface DistanceCalculationStrategy {

    double calculateDistance(double lat1, double lng1, double lat2, double lng2);

    String getStrategyName();
}
