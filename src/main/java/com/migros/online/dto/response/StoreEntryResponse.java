package com.migros.online.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreEntryResponse {

    private Long id;

    private String courierId;

    private String storeName;

    private LocalDateTime entryTime;

    private Double distanceFromStore;
}
