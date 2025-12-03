package com.migros.online.service.courier;

import com.migros.online.dto.request.CourierLocationRequest;
import com.migros.online.dto.response.CourierLocationResponse;
import com.migros.online.dto.response.TotalDistanceResponse;
import com.migros.online.entity.CourierLocation;
import com.migros.online.entity.Store;
import com.migros.online.mapper.CourierLocationMapper;
import com.migros.online.repository.CourierLocationRepository;
import com.migros.online.repository.StoreEntryRepository;
import com.migros.online.repository.StoreRepository;
import com.migros.online.service.distance.DistanceCalculatorService;
import com.migros.online.service.store.StoreEntryNotificationService;
import com.migros.online.service.store.observer.StoreEntryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourierLocationService {

    private static final double STORE_RADIUS_METERS = 100.0;
    private static final long REENTRY_COOLDOWN_SECONDS = 60;

    private final CourierLocationRepository locationRepository;
    private final StoreEntryRepository storeEntryRepository;
    private final StoreRepository storeRepository;
    private final DistanceCalculatorService distanceCalculatorService;
    private final StoreEntryNotificationService notificationService;
    private final CourierLocationMapper locationMapper;

    @Transactional
    public CourierLocationResponse processLocation(CourierLocationRequest request) {
        log.info("Processing location for courier: {} at ({}, {}) at time {}", 
                request.getCourierId(), request.getLat(), request.getLng(), request.getTime());

        String courierId = request.getCourierId();


        CourierLocation newLocation = locationMapper.toEntity(request);
        CourierLocation savedLocation = locationRepository.save(newLocation);

        List<String> storeEntriesLogged = checkStoreProximity(courierId, request);

        CourierLocationResponse response = locationMapper.toResponse(savedLocation);
        
        if (!storeEntriesLogged.isEmpty()) {
            response.setMessage("Location recorded. Entered store radius: " + String.join(", ", storeEntriesLogged));
        } else {
            response.setMessage("Location recorded successfully");
        }

        return response;
    }

    private List<String> checkStoreProximity(String courierId, CourierLocationRequest request) {
        List<String> enteredStores = new ArrayList<>();
        List<Store> stores = storeRepository.findAll();

        for (Store store : stores) {
            double distance = distanceCalculatorService.calculateDistance(
                    request.getLat(), request.getLng(),
                    store.getLat(), store.getLng()
            );

            if (distance <= STORE_RADIUS_METERS) {
                LocalDateTime cooldownThreshold = request.getTime().minusSeconds(REENTRY_COOLDOWN_SECONDS);
                boolean recentEntry = storeEntryRepository.existsByCourierIdAndStoreAndEntryTimeAfter(
                        courierId, store, cooldownThreshold);

                if (!recentEntry) {
                    log.info("Logging store entry for courier {} at store {}", 
                            courierId, store.getName());

                    StoreEntryEvent event = StoreEntryEvent.builder()
                            .courierId(courierId)
                            .store(store)
                            .lat(request.getLat())
                            .lng(request.getLng())
                            .distanceFromStore(Math.round(distance * 100.0) / 100.0)
                            .entryTime(request.getTime())
                            .build();

                    notificationService.notifyObservers(event);
                    enteredStores.add(store.getName());
                }
            }
        }

        return enteredStores;
    }

    @Transactional(readOnly = true)
    public List<CourierLocationResponse> getCourierLocations(String courierId) {
        List<CourierLocation> locations = locationRepository.findByCourierIdOrderByTimestampAsc(courierId);
        return locationMapper.toResponseList(locations);
    }

    @Transactional(readOnly = true)
    public Optional<CourierLocationResponse> getLatestLocation(String courierId) {
        return locationRepository.findTopByCourierIdOrderByTimestampDesc(courierId)
                .map(locationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TotalDistanceResponse getTotalTravelDistance(String courierId) {
        List<CourierLocation> locations = locationRepository.findByCourierIdOrderByTimestampAsc(courierId);
        
        double totalDistance = 0.0;
        for (int i = 1; i < locations.size(); i++) {
            CourierLocation prev = locations.get(i - 1);
            CourierLocation curr = locations.get(i);
            totalDistance += distanceCalculatorService.calculateDistance(
                    prev.getLat(), prev.getLng(),
                    curr.getLat(), curr.getLng()
            );
        }

        String formattedDistance = formatDistance(totalDistance);
        
        log.info("Total travel distance for courier {}: {} meters ({})", 
                courierId, totalDistance, formattedDistance);
        
        return TotalDistanceResponse.builder()
                .courierId(courierId)
                .totalDistance(totalDistance)
                .formattedDistance(formattedDistance)
                .build();
    }

    private String formatDistance(Double distanceMeters) {
        if (distanceMeters == null || distanceMeters == 0) {
            return "0 m";
        }
        if (distanceMeters >= 1000) {
            return String.format("%.2f km", distanceMeters / 1000);
        }
        return String.format("%.2f m", distanceMeters);
    }

    @Transactional(readOnly = true)
    public long getLocationCount() {
        return locationRepository.count();
    }
}
