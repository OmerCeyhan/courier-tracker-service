package com.migros.online.service.store;

import com.migros.online.dto.response.StoreEntryResponse;
import com.migros.online.entity.Store;
import com.migros.online.entity.StoreEntry;
import com.migros.online.exception.ResourceNotFoundException;
import com.migros.online.mapper.StoreEntryMapper;
import com.migros.online.repository.StoreEntryRepository;
import com.migros.online.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreEntryService {

    private final StoreEntryRepository storeEntryRepository;
    private final StoreRepository storeRepository;
    private final StoreEntryMapper storeEntryMapper;

    @Transactional(readOnly = true)
    public List<StoreEntryResponse> getEntriesByCourier(String courierId) {
        List<StoreEntry> entries = storeEntryRepository.findByCourierIdOrderByEntryTimeDesc(courierId);
        return storeEntryMapper.toResponseList(entries);
    }

    @Transactional(readOnly = true)
    public List<StoreEntryResponse> getEntriesByStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));
        
        List<StoreEntry> entries = storeEntryRepository.findByStoreOrderByEntryTimeDesc(store);
        return storeEntryMapper.toResponseList(entries);
    }

    @Transactional(readOnly = true)
    public List<StoreEntryResponse> getAllEntries() {
        List<StoreEntry> entries = storeEntryRepository.findAll();
        return storeEntryMapper.toResponseList(entries);
    }

    @Transactional(readOnly = true)
    public long getEntryCountByCourier(String courierId) {
        return storeEntryRepository.countByCourierId(courierId);
    }

    @Transactional(readOnly = true)
    public long getTotalEntryCount() {
        return storeEntryRepository.count();
    }
}
