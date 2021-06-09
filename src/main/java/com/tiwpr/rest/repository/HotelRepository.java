package com.tiwpr.rest.repository;

import com.tiwpr.rest.model.dao.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    Optional<Hotel> findByHotelId(long hotelId);
    Optional<Hotel> findByAddress(String address);
}
