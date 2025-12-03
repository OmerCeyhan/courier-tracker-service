package com.migros.online;

import com.migros.online.mapper.CourierLocationMapper;
import com.migros.online.mapper.StoreEntryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


@SpringBootTest
@DisplayName("Application Context Integration Tests")
class CourirerTrackerServiceApplicationTests {

    @MockBean
    private CourierLocationMapper courierLocationMapper;

    @MockBean
    private StoreEntryMapper storeEntryMapper;

    @Test
    void contextLoads() {
    }

}
