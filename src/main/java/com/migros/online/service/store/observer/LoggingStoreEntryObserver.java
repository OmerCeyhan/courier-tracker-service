package com.migros.online.service.store.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingStoreEntryObserver implements StoreEntryObserver {

    @Override
    public void onStoreEntry(StoreEntryEvent event) {
        log.info("=== STORE ENTRY DETECTED ===");
        log.info("Courier ID: {}", event.getCourierId());
        log.info("Store: {}", event.getStore().getName());
        log.info("Entry Time: {}", event.getEntryTime());
        log.info("Distance from Store: {} meters", String.format("%.2f", event.getDistanceFromStore()));
        log.info("Courier Location: ({}, {})", event.getLat(), event.getLng());
        log.info("Store Location: ({}, {})", event.getStore().getLat(), event.getStore().getLng());
        log.info("============================");
    }

    @Override
    public String getObserverName() {
        return "LoggingObserver";
    }
}
