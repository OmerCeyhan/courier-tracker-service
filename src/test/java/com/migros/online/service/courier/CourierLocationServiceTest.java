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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourierLocationService Unit Tests")
class CourierLocationServiceTest {

    @Mock
    private CourierLocationRepository locationRepository;

    @Mock
    private StoreEntryRepository storeEntryRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private DistanceCalculatorService distanceCalculatorService;

    @Mock
    private StoreEntryNotificationService notificationService;

    @Mock
    private CourierLocationMapper locationMapper;

    @InjectMocks
    private CourierLocationService courierLocationService;

    private CourierLocationRequest testRequest;
    private CourierLocation testLocation;
    private CourierLocationResponse testResponse;
    private Store testStore;

    @BeforeEach
    void setUp() {
        testRequest = CourierLocationRequest.builder()
                .courierId("test-courier-1")
                .lat(40.9923307)
                .lng(29.1244229)
                .time(LocalDateTime.now())
                .build();

        testLocation = CourierLocation.builder()
                .id(1L)
                .courierId("test-courier-1")
                .lat(40.9923307)
                .lng(29.1244229)
                .timestamp(LocalDateTime.now())
                .build();

        testResponse = CourierLocationResponse.builder()
                .id(1L)
                .courierId("test-courier-1")
                .lat(40.9923307)
                .lng(29.1244229)
                .timestamp(LocalDateTime.now())
                .build();

        testStore = Store.builder()
                .id(1L)
                .name("Ataşehir MMM Migros")
                .lat(40.9923307)
                .lng(29.1244229)
                .build();
    }

    @Test
    @DisplayName("Should process location and save to repository")
    void shouldProcessLocationAndSaveToRepository() {
        when(locationMapper.toEntity(testRequest)).thenReturn(testLocation);
        when(locationRepository.save(testLocation)).thenReturn(testLocation);
        when(locationMapper.toResponse(testLocation)).thenReturn(testResponse);
        when(storeRepository.findAll()).thenReturn(Collections.emptyList());

        CourierLocationResponse response = courierLocationService.processLocation(testRequest);

        assertNotNull(response);
        assertEquals("Location recorded successfully", response.getMessage());
        verify(locationRepository).save(testLocation);
    }

    @Test
    @DisplayName("Should calculate distance from previous location when computing total distance")
    void shouldCalculateDistanceFromPreviousLocation() {
        CourierLocation location1 = CourierLocation.builder()
                .id(1L)
                .courierId("test-courier-1")
                .lat(40.99)
                .lng(29.12)
                .timestamp(LocalDateTime.now().minusMinutes(5))
                .build();

        CourierLocation location2 = CourierLocation.builder()
                .id(2L)
                .courierId("test-courier-1")
                .lat(40.9923307)
                .lng(29.1244229)
                .timestamp(LocalDateTime.now())
                .build();

        when(locationRepository.findByCourierIdOrderByTimestampAsc("test-courier-1"))
                .thenReturn(Arrays.asList(location1, location2));
        when(distanceCalculatorService.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(500.0);

        courierLocationService.getTotalTravelDistance("test-courier-1");

        verify(distanceCalculatorService).calculateDistance(
                location1.getLat(), location1.getLng(),
                location2.getLat(), location2.getLng()
        );
    }

    @Test
    @DisplayName("Should detect store entry when within 100 meters")
    void shouldDetectStoreEntryWhenWithin100Meters() {
        when(locationMapper.toEntity(testRequest)).thenReturn(testLocation);
        when(locationRepository.save(testLocation)).thenReturn(testLocation);
        when(locationMapper.toResponse(testLocation)).thenReturn(testResponse);
        when(storeRepository.findAll()).thenReturn(Collections.singletonList(testStore));
        when(distanceCalculatorService.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(50.0); // Within 100m radius
        when(storeEntryRepository.existsByCourierIdAndStoreAndEntryTimeAfter(anyString(), any(), any()))
                .thenReturn(false);

        CourierLocationResponse response = courierLocationService.processLocation(testRequest);

        assertTrue(response.getMessage().contains("Ataşehir"));
        verify(notificationService).notifyObservers(any(StoreEntryEvent.class));
    }

    @Test
    @DisplayName("Should not log store entry when outside 100 meters")
    void shouldNotLogStoreEntryWhenOutside100Meters() {
        when(locationMapper.toEntity(testRequest)).thenReturn(testLocation);
        when(locationRepository.save(testLocation)).thenReturn(testLocation);
        when(locationMapper.toResponse(testLocation)).thenReturn(testResponse);
        when(storeRepository.findAll()).thenReturn(Collections.singletonList(testStore));
        when(distanceCalculatorService.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(150.0); // Outside 100m radius

        CourierLocationResponse response = courierLocationService.processLocation(testRequest);

        assertEquals("Location recorded successfully", response.getMessage());
        verify(notificationService, never()).notifyObservers(any(StoreEntryEvent.class));
    }

    @Test
    @DisplayName("Should respect reentry cooldown period")
    void shouldRespectReentryCooldownPeriod() {
        when(locationMapper.toEntity(testRequest)).thenReturn(testLocation);
        when(locationRepository.save(testLocation)).thenReturn(testLocation);
        when(locationMapper.toResponse(testLocation)).thenReturn(testResponse);
        when(storeRepository.findAll()).thenReturn(Collections.singletonList(testStore));
        when(distanceCalculatorService.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(50.0);
        when(storeEntryRepository.existsByCourierIdAndStoreAndEntryTimeAfter(anyString(), any(), any()))
                .thenReturn(true); // Recent entry exists

        CourierLocationResponse response = courierLocationService.processLocation(testRequest);

        assertEquals("Location recorded successfully", response.getMessage());
        verify(notificationService, never()).notifyObservers(any(StoreEntryEvent.class));
    }

    @Test
    @DisplayName("Should get courier locations")
    void shouldGetCourierLocations() {
        List<CourierLocation> locations = Arrays.asList(testLocation);
        List<CourierLocationResponse> responses = Arrays.asList(testResponse);

        when(locationRepository.findByCourierIdOrderByTimestampAsc("test-courier-1")).thenReturn(locations);
        when(locationMapper.toResponseList(locations)).thenReturn(responses);

        List<CourierLocationResponse> result = courierLocationService.getCourierLocations("test-courier-1");

        assertEquals(1, result.size());
        assertEquals("test-courier-1", result.get(0).getCourierId());
    }

    @Test
    @DisplayName("Should get latest location")
    void shouldGetLatestLocation() {
        when(locationRepository.findTopByCourierIdOrderByTimestampDesc("test-courier-1"))
                .thenReturn(Optional.of(testLocation));
        when(locationMapper.toResponse(testLocation)).thenReturn(testResponse);

        Optional<CourierLocationResponse> result = courierLocationService.getLatestLocation("test-courier-1");

        assertTrue(result.isPresent());
        assertEquals("test-courier-1", result.get().getCourierId());
    }

    @Test
    @DisplayName("Should return empty when no latest location found")
    void shouldReturnEmptyWhenNoLatestLocationFound() {
        when(locationRepository.findTopByCourierIdOrderByTimestampDesc("unknown-courier"))
                .thenReturn(Optional.empty());

        Optional<CourierLocationResponse> result = courierLocationService.getLatestLocation("unknown-courier");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should calculate total travel distance")
    void shouldCalculateTotalTravelDistance() {
        CourierLocation location1 = CourierLocation.builder()
                .id(1L).courierId("test-courier-1").lat(40.99).lng(29.12).timestamp(LocalDateTime.now().minusMinutes(10)).build();
        CourierLocation location2 = CourierLocation.builder()
                .id(2L).courierId("test-courier-1").lat(40.98).lng(29.11).timestamp(LocalDateTime.now().minusMinutes(5)).build();
        CourierLocation location3 = CourierLocation.builder()
                .id(3L).courierId("test-courier-1").lat(40.97).lng(29.10).timestamp(LocalDateTime.now()).build();

        List<CourierLocation> locations = Arrays.asList(location1, location2, location3);

        when(locationRepository.findByCourierIdOrderByTimestampAsc("test-courier-1")).thenReturn(locations);
        when(distanceCalculatorService.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(500.0); // 500m between each point

        TotalDistanceResponse result = courierLocationService.getTotalTravelDistance("test-courier-1");

        assertNotNull(result);
        assertEquals("test-courier-1", result.getCourierId());
        assertEquals(1000.0, result.getTotalDistance()); // 2 segments * 500m
        verify(distanceCalculatorService, times(2)).calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Should return zero distance when no locations")
    void shouldReturnZeroDistanceWhenNoLocations() {
        when(locationRepository.findByCourierIdOrderByTimestampAsc("test-courier-1"))
                .thenReturn(Collections.emptyList());

        TotalDistanceResponse result = courierLocationService.getTotalTravelDistance("test-courier-1");

        assertNotNull(result);
        assertEquals(0.0, result.getTotalDistance());
        assertEquals("0 m", result.getFormattedDistance());
    }

    @Test
    @DisplayName("Should return zero distance when single location")
    void shouldReturnZeroDistanceWhenSingleLocation() {
        when(locationRepository.findByCourierIdOrderByTimestampAsc("test-courier-1"))
                .thenReturn(Collections.singletonList(testLocation));

        TotalDistanceResponse result = courierLocationService.getTotalTravelDistance("test-courier-1");

        assertNotNull(result);
        assertEquals(0.0, result.getTotalDistance());
    }

    @Test
    @DisplayName("Should format distance in kilometers when >= 1000m")
    void shouldFormatDistanceInKilometersWhenGreaterThanOrEqual1000m() {
        CourierLocation location1 = CourierLocation.builder()
                .id(1L).courierId("test-courier-1").lat(40.99).lng(29.12).timestamp(LocalDateTime.now().minusMinutes(10)).build();
        CourierLocation location2 = CourierLocation.builder()
                .id(2L).courierId("test-courier-1").lat(40.98).lng(29.11).timestamp(LocalDateTime.now()).build();

        when(locationRepository.findByCourierIdOrderByTimestampAsc("test-courier-1"))
                .thenReturn(Arrays.asList(location1, location2));
        when(distanceCalculatorService.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(1500.0);

        TotalDistanceResponse result = courierLocationService.getTotalTravelDistance("test-courier-1");

        assertEquals("1.50 km", result.getFormattedDistance());
    }

    @Test
    @DisplayName("Should get location count")
    void shouldGetLocationCount() {
        when(locationRepository.count()).thenReturn(10L);

        long count = courierLocationService.getLocationCount();

        assertEquals(10L, count);
    }

    @Test
    @DisplayName("Should create store entry event with correct data")
    void shouldCreateStoreEntryEventWithCorrectData() {
        when(locationMapper.toEntity(testRequest)).thenReturn(testLocation);
        when(locationRepository.save(testLocation)).thenReturn(testLocation);
        when(locationMapper.toResponse(testLocation)).thenReturn(testResponse);
        when(storeRepository.findAll()).thenReturn(Collections.singletonList(testStore));
        when(distanceCalculatorService.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(50.0);
        when(storeEntryRepository.existsByCourierIdAndStoreAndEntryTimeAfter(anyString(), any(), any()))
                .thenReturn(false);

        courierLocationService.processLocation(testRequest);

        ArgumentCaptor<StoreEntryEvent> eventCaptor = ArgumentCaptor.forClass(StoreEntryEvent.class);
        verify(notificationService).notifyObservers(eventCaptor.capture());

        StoreEntryEvent capturedEvent = eventCaptor.getValue();
        assertEquals("test-courier-1", capturedEvent.getCourierId());
        assertEquals(testStore, capturedEvent.getStore());
        assertEquals(50.0, capturedEvent.getDistanceFromStore());
    }

    @Test
    @DisplayName("Should handle multiple stores proximity check")
    void shouldHandleMultipleStoresProximityCheck() {
        Store store2 = Store.builder()
                .id(2L)
                .name("Beylikdüzü Migros")
                .lat(41.0551273)
                .lng(28.9343891)
                .build();

        when(locationMapper.toEntity(testRequest)).thenReturn(testLocation);
        when(locationRepository.save(testLocation)).thenReturn(testLocation);
        when(locationMapper.toResponse(testLocation)).thenReturn(testResponse);
        when(storeRepository.findAll()).thenReturn(Arrays.asList(testStore, store2));
        // First store is within range, second is not
        when(distanceCalculatorService.calculateDistance(
                testRequest.getLat(), testRequest.getLng(),
                testStore.getLat(), testStore.getLng()))
                .thenReturn(50.0);
        when(distanceCalculatorService.calculateDistance(
                testRequest.getLat(), testRequest.getLng(),
                store2.getLat(), store2.getLng()))
                .thenReturn(15000.0);
        when(storeEntryRepository.existsByCourierIdAndStoreAndEntryTimeAfter(anyString(), eq(testStore), any()))
                .thenReturn(false);

        CourierLocationResponse response = courierLocationService.processLocation(testRequest);

        assertTrue(response.getMessage().contains("Ataşehir"));
        assertFalse(response.getMessage().contains("Beylikdüzü"));
        verify(notificationService, times(1)).notifyObservers(any(StoreEntryEvent.class));
    }
}

