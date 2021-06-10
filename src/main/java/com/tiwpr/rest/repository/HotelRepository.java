package com.tiwpr.rest.repository;

import com.tiwpr.rest.model.dao.Hotel;
import com.tiwpr.rest.model.dao.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    Optional<Hotel> findByHotelId(long hotelId);

    Optional<Hotel> findByAddress(String address);

    Optional<Hotel> findHotelByRooms(Room room);
}
