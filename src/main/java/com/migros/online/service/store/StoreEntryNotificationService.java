package com.migros.online.service.store;

import com.migros.online.service.store.observer.StoreEntryEvent;
import com.migros.online.service.store.observer.StoreEntryObserver;
import com.migros.online.service.store.observer.StoreEntrySubject;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreEntryNotificationService implements StoreEntrySubject {

    private final List<StoreEntryObserver> observers = new ArrayList<>();
    private final List<StoreEntryObserver> availableObservers;

    @PostConstruct
    public void init() {
        log.info("Initializing StoreEntryNotificationService with {} observers", availableObservers.size());
        for (StoreEntryObserver observer : availableObservers) {
            registerObserver(observer);
        }
    }

    @Override
    public void registerObserver(StoreEntryObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            log.info("Registered observer: {}", observer.getObserverName());
        }
    }

    @Override
    public void removeObserver(StoreEntryObserver observer) {
        if (observers.remove(observer)) {
            log.info("Removed observer: {}", observer.getObserverName());
        }
    }

    @Override
    public void notifyObservers(StoreEntryEvent event) {
        log.info("Notifying {} observers of store entry event for courier {} at store {}",
                observers.size(),
                event.getCourierId(),
                event.getStore().getName());

        for (StoreEntryObserver observer : observers) {
            try {
                observer.onStoreEntry(event);
            } catch (Exception e) {
                log.error("Observer {} failed to process event: {}", 
                        observer.getObserverName(), e.getMessage(), e);
            }
        }
    }

    public int getObserverCount() {
        return observers.size();
    }

    public List<String> getObserverNames() {
        return observers.stream()
                .map(StoreEntryObserver::getObserverName)
                .toList();
    }
}
