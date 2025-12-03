package com.migros.online.controller;

import com.migros.online.dto.request.CourierLocationRequest;
import com.migros.online.dto.response.Response;
import com.migros.online.dto.response.CourierLocationResponse;
import com.migros.online.dto.response.TotalDistanceResponse;
import com.migros.online.service.courier.CourierLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/courier/location")
@RequiredArgsConstructor
@Validated
public class CourierLocationController {

    private final CourierLocationService locationService;

    @PostMapping
    public ResponseEntity<Response<CourierLocationResponse>> reportLocation(
            @Valid @RequestBody CourierLocationRequest request) {
        log.info("Received location update for courier: {} at ({}, {})",
                request.getCourierId(), request.getLat(), request.getLng());
        CourierLocationResponse response = locationService.processLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success("Location processed successfully", response));
    }

    @GetMapping("/courier/{courierId}/total-distance")
    public ResponseEntity<Response<TotalDistanceResponse>> getTotalTravelDistance(
            @PathVariable @UUID(message = "Courier ID must be a valid UUID") String courierId) {
        log.info("Received request to get total distance for courier: {}", courierId);
        TotalDistanceResponse response = locationService.getTotalTravelDistance(courierId);
        return ResponseEntity.ok(Response.success("Total travel distance retrieved", response));
    }

    @GetMapping("/courier/{courierId}")
    public ResponseEntity<Response<List<CourierLocationResponse>>> getCourierLocations(
            @PathVariable @UUID(message = "Courier ID must be a valid UUID") String courierId) {
        log.info("Received request to get locations for courier: {}", courierId);
        List<CourierLocationResponse> locations = locationService.getCourierLocations(courierId);
        return ResponseEntity.ok(Response.success(
                "Retrieved " + locations.size() + " locations", locations));
    }

    @GetMapping("/courier/{courierId}/latest")
    public ResponseEntity<Response<CourierLocationResponse>> getLatestLocation(
            @PathVariable @UUID(message = "Courier ID must be a valid UUID") String courierId) {
        log.info("Received request to get latest location for courier: {}", courierId);
        return locationService.getLatestLocation(courierId)
                .map(location -> ResponseEntity.ok(Response.success("Latest location retrieved", location)))
                .orElse(ResponseEntity.ok(Response.success("No locations recorded for this courier", null)));
    }

}
