package com.migros.online.repository;

import com.migros.online.entity.Store;
import com.migros.online.entity.StoreEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoreEntryRepository extends JpaRepository<StoreEntry, Long> {

    List<StoreEntry> findByCourierIdOrderByEntryTimeDesc(String courierId);

    List<StoreEntry> findByStoreOrderByEntryTimeDesc(Store store);


    @Query("SELECT COUNT(se) > 0 FROM StoreEntry se WHERE se.courierId = :courierId " +
           "AND se.store = :store AND se.entryTime > :sinceTime")
    boolean existsByCourierIdAndStoreAndEntryTimeAfter(
            @Param("courierId") String courierId,
            @Param("store") Store store,
            @Param("sinceTime") LocalDateTime sinceTime);

    long countByCourierId(String courierId);
}
