package com.migros.online.service.distance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DistanceCalculatorService {

    private DistanceCalculationStrategy strategy;

    public DistanceCalculatorService(@Qualifier("haversineDistanceStrategy") DistanceCalculationStrategy defaultStrategy) {
        this.strategy = defaultStrategy;
        log.info("DistanceCalculatorService initialized with {} strategy", strategy.getStrategyName());
    }

    public void setStrategy(DistanceCalculationStrategy strategy) {
        log.info("Switching distance calculation strategy from {} to {}", 
                this.strategy.getStrategyName(), strategy.getStrategyName());
        this.strategy = strategy;
    }

    public DistanceCalculationStrategy getStrategy() {
        return this.strategy;
    }

    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        return strategy.calculateDistance(lat1, lng1, lat2, lng2);
    }
}
