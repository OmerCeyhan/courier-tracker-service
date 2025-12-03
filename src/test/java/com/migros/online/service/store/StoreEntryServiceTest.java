package com.migros.online.service.store;

import com.migros.online.dto.response.StoreEntryResponse;
import com.migros.online.entity.Store;
import com.migros.online.entity.StoreEntry;
import com.migros.online.exception.ResourceNotFoundException;
import com.migros.online.mapper.StoreEntryMapper;
import com.migros.online.repository.StoreEntryRepository;
import com.migros.online.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreEntryService Unit Tests")
class StoreEntryServiceTest {

    @Mock
    private StoreEntryRepository storeEntryRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreEntryMapper storeEntryMapper;

    @InjectMocks
    private StoreEntryService storeEntryService;

    private Store testStore;
    private StoreEntry testEntry;
    private StoreEntryResponse testResponse;

    @BeforeEach
    void setUp() {
        testStore = Store.builder()
                .id(1L)
                .name("Ataşehir MMM Migros")
                .lat(40.9923307)
                .lng(29.1244229)
                .build();

        testEntry = StoreEntry.builder()
                .id(1L)
                .courierId("test-courier-1")
                .store(testStore)
                .entryTime(LocalDateTime.now())
                .build();

        testResponse = StoreEntryResponse.builder()
                .id(1L)
                .courierId("test-courier-1")
                .storeName("Ataşehir MMM Migros")
                .entryTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get entries by courier")
    void shouldGetEntriesByCourier() {
        List<StoreEntry> entries = Arrays.asList(testEntry);
        List<StoreEntryResponse> responses = Arrays.asList(testResponse);

        when(storeEntryRepository.findByCourierIdOrderByEntryTimeDesc("test-courier-1")).thenReturn(entries);
        when(storeEntryMapper.toResponseList(entries)).thenReturn(responses);

        List<StoreEntryResponse> result = storeEntryService.getEntriesByCourier("test-courier-1");

        assertEquals(1, result.size());
        assertEquals("test-courier-1", result.get(0).getCourierId());
        verify(storeEntryRepository).findByCourierIdOrderByEntryTimeDesc("test-courier-1");
    }

    @Test
    @DisplayName("Should return empty list when courier has no entries")
    void shouldReturnEmptyListWhenCourierHasNoEntries() {
        when(storeEntryRepository.findByCourierIdOrderByEntryTimeDesc("unknown-courier"))
                .thenReturn(Collections.emptyList());
        when(storeEntryMapper.toResponseList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        List<StoreEntryResponse> result = storeEntryService.getEntriesByCourier("unknown-courier");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should get entries by store")
    void shouldGetEntriesByStore() {
        List<StoreEntry> entries = Arrays.asList(testEntry);
        List<StoreEntryResponse> responses = Arrays.asList(testResponse);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(storeEntryRepository.findByStoreOrderByEntryTimeDesc(testStore)).thenReturn(entries);
        when(storeEntryMapper.toResponseList(entries)).thenReturn(responses);

        List<StoreEntryResponse> result = storeEntryService.getEntriesByStore(1L);

        assertEquals(1, result.size());
        assertEquals("Ataşehir MMM Migros", result.get(0).getStoreName());
    }

    @Test
    @DisplayName("Should throw exception when store not found")
    void shouldThrowExceptionWhenStoreNotFound() {
        when(storeRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> storeEntryService.getEntriesByStore(999L)
        );

        assertTrue(exception.getMessage().contains("999"));
        verify(storeEntryRepository, never()).findByStoreOrderByEntryTimeDesc(any());
    }

    @Test
    @DisplayName("Should get all entries")
    void shouldGetAllEntries() {
        StoreEntry entry2 = StoreEntry.builder()
                .id(2L)
                .courierId("test-courier-2")
                .store(testStore)
                .entryTime(LocalDateTime.now().minusMinutes(5))
                .build();

        StoreEntryResponse response2 = StoreEntryResponse.builder()
                .id(2L)
                .courierId("test-courier-2")
                .storeName("Ataşehir MMM Migros")
                .entryTime(LocalDateTime.now().minusMinutes(5))
                .build();

        List<StoreEntry> entries = Arrays.asList(testEntry, entry2);
        List<StoreEntryResponse> responses = Arrays.asList(testResponse, response2);

        when(storeEntryRepository.findAll()).thenReturn(entries);
        when(storeEntryMapper.toResponseList(entries)).thenReturn(responses);

        List<StoreEntryResponse> result = storeEntryService.getAllEntries();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no entries exist")
    void shouldReturnEmptyListWhenNoEntriesExist() {
        when(storeEntryRepository.findAll()).thenReturn(Collections.emptyList());
        when(storeEntryMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<StoreEntryResponse> result = storeEntryService.getAllEntries();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should get entry count by courier")
    void shouldGetEntryCountByCourier() {
        when(storeEntryRepository.countByCourierId("test-courier-1")).thenReturn(5L);

        long count = storeEntryService.getEntryCountByCourier("test-courier-1");

        assertEquals(5L, count);
    }

    @Test
    @DisplayName("Should return zero count when courier has no entries")
    void shouldReturnZeroCountWhenCourierHasNoEntries() {
        when(storeEntryRepository.countByCourierId("unknown-courier")).thenReturn(0L);

        long count = storeEntryService.getEntryCountByCourier("unknown-courier");

        assertEquals(0L, count);
    }

    @Test
    @DisplayName("Should get total entry count")
    void shouldGetTotalEntryCount() {
        when(storeEntryRepository.count()).thenReturn(100L);

        long count = storeEntryService.getTotalEntryCount();

        assertEquals(100L, count);
    }

    @Test
    @DisplayName("Should handle multiple couriers with entries")
    void shouldHandleMultipleCouriersWithEntries() {
        StoreEntry entry1 = StoreEntry.builder()
                .id(1L).courierId("courier-1").store(testStore).entryTime(LocalDateTime.now()).build();
        StoreEntry entry2 = StoreEntry.builder()
                .id(2L).courierId("courier-2").store(testStore).entryTime(LocalDateTime.now()).build();
        StoreEntry entry3 = StoreEntry.builder()
                .id(3L).courierId("courier-1").store(testStore).entryTime(LocalDateTime.now().minusMinutes(5)).build();

        List<StoreEntry> courier1Entries = Arrays.asList(entry1, entry3);
        List<StoreEntry> courier2Entries = Arrays.asList(entry2);

        when(storeEntryRepository.findByCourierIdOrderByEntryTimeDesc("courier-1")).thenReturn(courier1Entries);
        when(storeEntryRepository.findByCourierIdOrderByEntryTimeDesc("courier-2")).thenReturn(courier2Entries);
        when(storeEntryMapper.toResponseList(any())).thenReturn(Collections.emptyList());

        storeEntryService.getEntriesByCourier("courier-1");
        storeEntryService.getEntriesByCourier("courier-2");

        verify(storeEntryRepository).findByCourierIdOrderByEntryTimeDesc("courier-1");
        verify(storeEntryRepository).findByCourierIdOrderByEntryTimeDesc("courier-2");
    }
}

