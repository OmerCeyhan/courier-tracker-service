package com.migros.online.service.store.observer;

import com.migros.online.entity.Store;
import com.migros.online.entity.StoreEntry;
import com.migros.online.repository.StoreEntryRepository;
import com.migros.online.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersistenceStoreEntryObserver implements StoreEntryObserver {

    private final StoreEntryRepository storeEntryRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional
    public void onStoreEntry(StoreEntryEvent event) {
        Store store = storeRepository.findById(event.getStore().getId())
                .orElseThrow(() -> new RuntimeException("Store not found"));
        
        StoreEntry entry = StoreEntry.builder()
                .courierId(event.getCourierId())
                .store(store)
                .entryTime(event.getEntryTime())
                .distanceFromStore(event.getDistanceFromStore())
                .build();

        storeEntryRepository.save(entry);
    }

    @Override
    public String getObserverName() {
        return "PersistenceObserver";
    }
}
