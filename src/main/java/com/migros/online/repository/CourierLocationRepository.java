package com.migros.online.repository;

import com.migros.online.entity.CourierLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourierLocationRepository extends JpaRepository<CourierLocation, Long> {

    List<CourierLocation> findByCourierIdOrderByTimestampAsc(String courierId);

    Optional<CourierLocation> findTopByCourierIdOrderByTimestampDesc(String courierId);
}
