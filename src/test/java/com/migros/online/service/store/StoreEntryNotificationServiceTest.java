package com.migros.online.service.store;

import com.migros.online.entity.Store;
import com.migros.online.service.store.observer.StoreEntryEvent;
import com.migros.online.service.store.observer.StoreEntryObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreEntryNotificationService Unit Tests")
class StoreEntryNotificationServiceTest {

    @Mock
    private StoreEntryObserver mockObserver1;

    @Mock
    private StoreEntryObserver mockObserver2;

    private StoreEntryNotificationService notificationService;
    private StoreEntryEvent testEvent;

    @BeforeEach
    void setUp() {
        notificationService = new StoreEntryNotificationService(Collections.emptyList());

        Store testStore = Store.builder()
                .id(1L)
                .name("Test Store")
                .lat(40.9923307)
                .lng(29.1244229)
                .build();

        testEvent = StoreEntryEvent.builder()
                .courierId("test-courier-1")
                .store(testStore)
                .lat(40.9923307)
                .lng(29.1244229)
                .distanceFromStore(50.0)
                .entryTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should register observer")
    void shouldRegisterObserver() {
        when(mockObserver1.getObserverName()).thenReturn("MockObserver1");

        notificationService.registerObserver(mockObserver1);

        assertEquals(1, notificationService.getObserverCount());
        assertTrue(notificationService.getObserverNames().contains("MockObserver1"));
    }

    @Test
    @DisplayName("Should not register same observer twice")
    void shouldNotRegisterSameObserverTwice() {
        when(mockObserver1.getObserverName()).thenReturn("MockObserver1");

        notificationService.registerObserver(mockObserver1);
        notificationService.registerObserver(mockObserver1);

        assertEquals(1, notificationService.getObserverCount());
    }

    @Test
    @DisplayName("Should remove observer")
    void shouldRemoveObserver() {
        when(mockObserver1.getObserverName()).thenReturn("MockObserver1");
        notificationService.registerObserver(mockObserver1);

        notificationService.removeObserver(mockObserver1);

        assertEquals(0, notificationService.getObserverCount());
    }

    @Test
    @DisplayName("Should handle removing non-existent observer")
    void shouldHandleRemovingNonExistentObserver() {
        when(mockObserver1.getObserverName()).thenReturn("MockObserver1");
        notificationService.registerObserver(mockObserver1);

        notificationService.removeObserver(mockObserver2);

        assertEquals(1, notificationService.getObserverCount());
    }

    @Test
    @DisplayName("Should notify all observers")
    void shouldNotifyAllObservers() {
        when(mockObserver1.getObserverName()).thenReturn("MockObserver1");
        when(mockObserver2.getObserverName()).thenReturn("MockObserver2");

        notificationService.registerObserver(mockObserver1);
        notificationService.registerObserver(mockObserver2);

        notificationService.notifyObservers(testEvent);

        verify(mockObserver1).onStoreEntry(testEvent);
        verify(mockObserver2).onStoreEntry(testEvent);
    }

    @Test
    @DisplayName("Should continue notifying other observers when one fails")
    void shouldContinueNotifyingOtherObserversWhenOneFails() {
        when(mockObserver1.getObserverName()).thenReturn("FailingObserver");
        when(mockObserver2.getObserverName()).thenReturn("WorkingObserver");
        doThrow(new RuntimeException("Observer failed")).when(mockObserver1).onStoreEntry(any());

        notificationService.registerObserver(mockObserver1);
        notificationService.registerObserver(mockObserver2);

        assertDoesNotThrow(() -> notificationService.notifyObservers(testEvent));

        verify(mockObserver1).onStoreEntry(testEvent);
        verify(mockObserver2).onStoreEntry(testEvent);
    }

    @Test
    @DisplayName("Should return correct observer count")
    void shouldReturnCorrectObserverCount() {
        when(mockObserver1.getObserverName()).thenReturn("MockObserver1");
        when(mockObserver2.getObserverName()).thenReturn("MockObserver2");

        assertEquals(0, notificationService.getObserverCount());

        notificationService.registerObserver(mockObserver1);
        assertEquals(1, notificationService.getObserverCount());

        notificationService.registerObserver(mockObserver2);
        assertEquals(2, notificationService.getObserverCount());
    }

    @Test
    @DisplayName("Should return observer names")
    void shouldReturnObserverNames() {
        when(mockObserver1.getObserverName()).thenReturn("Observer1");
        when(mockObserver2.getObserverName()).thenReturn("Observer2");

        notificationService.registerObserver(mockObserver1);
        notificationService.registerObserver(mockObserver2);

        List<String> names = notificationService.getObserverNames();

        assertEquals(2, names.size());
        assertTrue(names.contains("Observer1"));
        assertTrue(names.contains("Observer2"));
    }

    @Test
    @DisplayName("Should initialize with available observers")
    void shouldInitializeWithAvailableObservers() {
        when(mockObserver1.getObserverName()).thenReturn("AvailableObserver1");
        when(mockObserver2.getObserverName()).thenReturn("AvailableObserver2");

        StoreEntryNotificationService serviceWithObservers = 
                new StoreEntryNotificationService(Arrays.asList(mockObserver1, mockObserver2));
        serviceWithObservers.init();

        assertEquals(2, serviceWithObservers.getObserverCount());
    }

    @Test
    @DisplayName("Should handle notification with no observers")
    void shouldHandleNotificationWithNoObservers() {
        assertDoesNotThrow(() -> notificationService.notifyObservers(testEvent));
    }
}

