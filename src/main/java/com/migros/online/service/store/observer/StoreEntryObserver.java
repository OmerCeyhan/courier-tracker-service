package com.migros.online.service.store.observer;

public interface StoreEntryObserver {

    void onStoreEntry(StoreEntryEvent event);

    String getObserverName();
}
