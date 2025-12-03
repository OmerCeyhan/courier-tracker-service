package com.migros.online.service.store.observer;

import com.migros.online.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreEntryEvent {

    private String courierId;
    private Store store;
    private Double lat;
    private Double lng;
    private Double distanceFromStore;
    private LocalDateTime entryTime;
}
