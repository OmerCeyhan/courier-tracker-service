package com.migros.online.controller;

import com.migros.online.dto.response.Response;
import com.migros.online.dto.response.StoreEntryResponse;
import com.migros.online.service.store.StoreEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/store-entries")
@RequiredArgsConstructor
@Validated
public class StoreEntryController {

    private final StoreEntryService storeEntryService;

    @GetMapping
    public ResponseEntity<Response<List<StoreEntryResponse>>> getAllEntries() {
        log.info("Received request to get all store entries");
        List<StoreEntryResponse> entries = storeEntryService.getAllEntries();
        return ResponseEntity.ok(Response.success(
                "Retrieved " + entries.size() + " store entries", entries));
    }

    @GetMapping("/courier/{courierId}")
    public ResponseEntity<Response<List<StoreEntryResponse>>> getEntriesByCourier(
            @PathVariable @UUID(message = "Courier ID must be a valid UUID") String courierId) {
        log.info("Received request to get store entries for courier: {}", courierId);
        List<StoreEntryResponse> entries = storeEntryService.getEntriesByCourier(courierId);
        return ResponseEntity.ok(Response.success(
                "Retrieved " + entries.size() + " entries for courier " + courierId, entries));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<Response<List<StoreEntryResponse>>> getEntriesByStore(
            @PathVariable Long storeId) {
        log.info("Received request to get store entries for store ID: {}", storeId);
        List<StoreEntryResponse> entries = storeEntryService.getEntriesByStore(storeId);
        return ResponseEntity.ok(Response.success(
                "Retrieved " + entries.size() + " entries for store " + storeId, entries));
    }
}
