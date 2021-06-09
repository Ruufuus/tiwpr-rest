package com.tiwpr.rest.repository;

import com.tiwpr.rest.model.dao.Hotel;
import com.tiwpr.rest.model.dao.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotel(Hotel hotel);

    Optional<Room> findByRoomId(long id);

    Optional<Room> findByHotelAndRoomId(Hotel hotel, Long roomId);

}
