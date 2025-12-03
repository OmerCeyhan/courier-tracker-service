package com.migros.online.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.migros.online.entity.Store;
import com.migros.online.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StoreRepository storeRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        if (isNotEmpty(storeRepository.findAll())) {
            log.info("Store list has already been initialized.Skipping initialization");
            return;
        }
        log.info("Initializing store data from stores.json.");
        try {
            ClassPathResource resource = new ClassPathResource("stores.json");
            InputStream inputStream = resource.getInputStream();

            List<Map<String, Object>> storesData = objectMapper.readValue(
                    inputStream,
                    new TypeReference<>() {
                    }
            );

            for (Map<String, Object> storeData : storesData) {
                String name = (String) storeData.get("name");
                Double lat = ((Number) storeData.get("lat")).doubleValue();
                Double lng = ((Number) storeData.get("lng")).doubleValue();
                Store store = Store.builder()
                        .name(name)
                        .lat(lat)
                        .lng(lng)
                        .build();
                storeRepository.save(store);
                log.info("Loaded store: {} at ({}, {})", name, lat, lng);
            }
            log.info("Store data initialization complete.");

        } catch (Exception e) {
            log.error("Failed to load store data from stores.json", e);
            throw e;
        }
    }
}
