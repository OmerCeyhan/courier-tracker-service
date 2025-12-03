package com.migros.online.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.migros.online.dto.request.CourierLocationRequest;
import com.migros.online.dto.response.CourierLocationResponse;
import com.migros.online.dto.response.TotalDistanceResponse;
import com.migros.online.service.courier.CourierLocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourierLocationController.class)
@DisplayName("CourierLocationController Unit Tests")
class CourierLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourierLocationService locationService;

    private ObjectMapper objectMapper;
    private CourierLocationRequest testRequest;
    private CourierLocationResponse testResponse;

    private static final String TEST_COURIER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String UNKNOWN_COURIER_ID = "550e8400-e29b-41d4-a716-446655440001";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testRequest = CourierLocationRequest.builder()
                .courierId(TEST_COURIER_ID)
                .lat(40.9923307)
                .lng(29.1244229)
                .time(LocalDateTime.now())
                .build();

        testResponse = CourierLocationResponse.builder()
                .id(1L)
                .courierId(TEST_COURIER_ID)
                .lat(40.9923307)
                .lng(29.1244229)
                .timestamp(LocalDateTime.now())
                .message("Location recorded successfully")
                .build();
    }

    @Test
    @DisplayName("Should report location successfully")
    void shouldReportLocationSuccessfully() throws Exception {
        when(locationService.processLocation(any(CourierLocationRequest.class))).thenReturn(testResponse);

        mockMvc.perform(post("/api/v1/courier/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Location processed successfully"))
                .andExpect(jsonPath("$.data.courierId").value(TEST_COURIER_ID));

        verify(locationService).processLocation(any(CourierLocationRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when courier ID is missing")
    void shouldReturn400WhenCourierIdIsMissing() throws Exception {
        CourierLocationRequest invalidRequest = CourierLocationRequest.builder()
                .lat(40.9923307)
                .lng(29.1244229)
                .time(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/v1/courier/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when latitude is out of range")
    void shouldReturn400WhenLatitudeIsOutOfRange() throws Exception {
        CourierLocationRequest invalidRequest = CourierLocationRequest.builder()
                .courierId(TEST_COURIER_ID)
                .lat(100.0) // Invalid latitude
                .lng(29.1244229)
                .time(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/v1/courier/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when longitude is out of range")
    void shouldReturn400WhenLongitudeIsOutOfRange() throws Exception {
        CourierLocationRequest invalidRequest = CourierLocationRequest.builder()
                .courierId(TEST_COURIER_ID)
                .lat(40.9923307)
                .lng(200.0) // Invalid longitude
                .time(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/v1/courier/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get courier locations")
    void shouldGetCourierLocations() throws Exception {
        List<CourierLocationResponse> responses = Arrays.asList(testResponse);
        when(locationService.getCourierLocations(TEST_COURIER_ID)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/courier/location/courier/" + TEST_COURIER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].courierId").value(TEST_COURIER_ID));
    }

    @Test
    @DisplayName("Should return empty list when courier has no locations")
    void shouldReturnEmptyListWhenCourierHasNoLocations() throws Exception {
        when(locationService.getCourierLocations(UNKNOWN_COURIER_ID)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/courier/location/courier/" + UNKNOWN_COURIER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("Should get latest location")
    void shouldGetLatestLocation() throws Exception {
        when(locationService.getLatestLocation(TEST_COURIER_ID)).thenReturn(Optional.of(testResponse));

        mockMvc.perform(get("/api/v1/courier/location/courier/" + TEST_COURIER_ID + "/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.courierId").value(TEST_COURIER_ID));
    }

    @Test
    @DisplayName("Should return null data when no latest location found")
    void shouldReturnNullDataWhenNoLatestLocationFound() throws Exception {
        when(locationService.getLatestLocation(UNKNOWN_COURIER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/courier/location/courier/" + UNKNOWN_COURIER_ID + "/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("No locations recorded for this courier"));
    }

    @Test
    @DisplayName("Should get total travel distance")
    void shouldGetTotalTravelDistance() throws Exception {
        TotalDistanceResponse distanceResponse = TotalDistanceResponse.builder()
                .courierId(TEST_COURIER_ID)
                .totalDistance(1500.0)
                .formattedDistance("1.50 km")
                .build();

        when(locationService.getTotalTravelDistance(TEST_COURIER_ID)).thenReturn(distanceResponse);

        mockMvc.perform(get("/api/v1/courier/location/courier/" + TEST_COURIER_ID + "/total-distance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDistance").value(1500.0))
                .andExpect(jsonPath("$.data.formattedDistance").value("1.50 km"));
    }

    @Test
    @DisplayName("Should return zero distance when courier has no locations")
    void shouldReturnZeroDistanceWhenCourierHasNoLocations() throws Exception {
        TotalDistanceResponse distanceResponse = TotalDistanceResponse.builder()
                .courierId(UNKNOWN_COURIER_ID)
                .totalDistance(0.0)
                .formattedDistance("0 m")
                .build();

        when(locationService.getTotalTravelDistance(UNKNOWN_COURIER_ID)).thenReturn(distanceResponse);

        mockMvc.perform(get("/api/v1/courier/location/courier/" + UNKNOWN_COURIER_ID + "/total-distance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalDistance").value(0.0))
                .andExpect(jsonPath("$.data.formattedDistance").value("0 m"));
    }

    @Test
    @DisplayName("Should report location with store entry message")
    void shouldReportLocationWithStoreEntryMessage() throws Exception {
        CourierLocationResponse responseWithEntry = CourierLocationResponse.builder()
                .id(1L)
                .courierId(TEST_COURIER_ID)
                .lat(40.9923307)
                .lng(29.1244229)
                .timestamp(LocalDateTime.now())
                .message("Location recorded. Entered store radius: Ataşehir MMM Migros")
                .build();

        when(locationService.processLocation(any(CourierLocationRequest.class))).thenReturn(responseWithEntry);

        mockMvc.perform(post("/api/v1/courier/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.message").value(containsString("Ataşehir")));
    }

    @Test
    @DisplayName("Should handle multiple location history")
    void shouldHandleMultipleLocationHistory() throws Exception {
        CourierLocationResponse response1 = CourierLocationResponse.builder()
                .id(1L).courierId(TEST_COURIER_ID).lat(40.99).lng(29.12).timestamp(LocalDateTime.now().minusMinutes(10)).build();
        CourierLocationResponse response2 = CourierLocationResponse.builder()
                .id(2L).courierId(TEST_COURIER_ID).lat(40.98).lng(29.11).timestamp(LocalDateTime.now().minusMinutes(5)).build();
        CourierLocationResponse response3 = CourierLocationResponse.builder()
                .id(3L).courierId(TEST_COURIER_ID).lat(40.97).lng(29.10).timestamp(LocalDateTime.now()).build();

        when(locationService.getCourierLocations(TEST_COURIER_ID))
                .thenReturn(Arrays.asList(response1, response2, response3));

        mockMvc.perform(get("/api/v1/courier/location/courier/" + TEST_COURIER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)));
    }
}

