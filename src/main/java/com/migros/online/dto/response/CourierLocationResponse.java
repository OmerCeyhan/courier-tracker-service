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
public class CourierLocationResponse {

    private Long id;

    private String courierId;

    private Double lat;

    private Double lng;

    private LocalDateTime timestamp;

    private String message;
}
