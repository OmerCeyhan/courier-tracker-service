package com.migros.online.service.store.observer;

public interface StoreEntrySubject {

    void registerObserver(StoreEntryObserver observer);

    void removeObserver(StoreEntryObserver observer);

    void notifyObservers(StoreEntryEvent event);
}
