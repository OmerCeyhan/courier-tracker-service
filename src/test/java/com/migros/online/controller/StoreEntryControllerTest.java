package com.migros.online.controller;

import com.migros.online.dto.response.StoreEntryResponse;
import com.migros.online.exception.ResourceNotFoundException;
import com.migros.online.service.store.StoreEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoreEntryController.class)
@DisplayName("StoreEntryController Unit Tests")
class StoreEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoreEntryService storeEntryService;

    private StoreEntryResponse testResponse;

    private static final String TEST_COURIER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String UNKNOWN_COURIER_ID = "550e8400-e29b-41d4-a716-446655440001";

    @BeforeEach
    void setUp() {
        testResponse = StoreEntryResponse.builder()
                .id(1L)
                .courierId(TEST_COURIER_ID)
                .storeName("Ataşehir MMM Migros")
                .entryTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get all entries")
    void shouldGetAllEntries() throws Exception {
        List<StoreEntryResponse> responses = Arrays.asList(testResponse);
        when(storeEntryService.getAllEntries()).thenReturn(responses);

        mockMvc.perform(get("/api/v1/store-entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].courierId").value(TEST_COURIER_ID));
    }

    @Test
    @DisplayName("Should return empty list when no entries exist")
    void shouldReturnEmptyListWhenNoEntriesExist() throws Exception {
        when(storeEntryService.getAllEntries()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/store-entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("Should get entries by courier")
    void shouldGetEntriesByCourier() throws Exception {
        List<StoreEntryResponse> responses = Arrays.asList(testResponse);
        when(storeEntryService.getEntriesByCourier(TEST_COURIER_ID)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/store-entries/courier/" + TEST_COURIER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].storeName").value("Ataşehir MMM Migros"));
    }

    @Test
    @DisplayName("Should return empty list when courier has no entries")
    void shouldReturnEmptyListWhenCourierHasNoEntries() throws Exception {
        when(storeEntryService.getEntriesByCourier(UNKNOWN_COURIER_ID)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/store-entries/courier/" + UNKNOWN_COURIER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("Should get entries by store")
    void shouldGetEntriesByStore() throws Exception {
        List<StoreEntryResponse> responses = Arrays.asList(testResponse);
        when(storeEntryService.getEntriesByStore(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/store-entries/store/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    @DisplayName("Should return 404 when store not found")
    void shouldReturn404WhenStoreNotFound() throws Exception {
        when(storeEntryService.getEntriesByStore(999L))
                .thenThrow(new ResourceNotFoundException("Store not found with ID: 999"));

        mockMvc.perform(get("/api/v1/store-entries/store/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle multiple entries response")
    void shouldHandleMultipleEntriesResponse() throws Exception {
        StoreEntryResponse response1 = StoreEntryResponse.builder()
                .id(1L).courierId(TEST_COURIER_ID).storeName("Store 1").entryTime(LocalDateTime.now()).build();
        StoreEntryResponse response2 = StoreEntryResponse.builder()
                .id(2L).courierId(UNKNOWN_COURIER_ID).storeName("Store 2").entryTime(LocalDateTime.now().minusMinutes(5)).build();
        StoreEntryResponse response3 = StoreEntryResponse.builder()
                .id(3L).courierId(TEST_COURIER_ID).storeName("Store 3").entryTime(LocalDateTime.now().minusMinutes(10)).build();

        when(storeEntryService.getAllEntries()).thenReturn(Arrays.asList(response1, response2, response3));

        mockMvc.perform(get("/api/v1/store-entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    @DisplayName("Should get multiple entries for same courier")
    void shouldGetMultipleEntriesForSameCourier() throws Exception {
        StoreEntryResponse entry1 = StoreEntryResponse.builder()
                .id(1L).courierId(TEST_COURIER_ID).storeName("Store 1").entryTime(LocalDateTime.now()).build();
        StoreEntryResponse entry2 = StoreEntryResponse.builder()
                .id(2L).courierId(TEST_COURIER_ID).storeName("Store 2").entryTime(LocalDateTime.now().minusMinutes(30)).build();

        when(storeEntryService.getEntriesByCourier(TEST_COURIER_ID))
                .thenReturn(Arrays.asList(entry1, entry2));

        mockMvc.perform(get("/api/v1/store-entries/courier/" + TEST_COURIER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.message").value(containsString("2 entries")));
    }
}

