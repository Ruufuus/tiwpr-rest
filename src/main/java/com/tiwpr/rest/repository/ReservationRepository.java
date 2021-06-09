package com.tiwpr.rest.repository;

import com.tiwpr.rest.model.dao.Client;
import com.tiwpr.rest.model.dao.Reservation;
import com.tiwpr.rest.model.dao.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByClients(Client client);

    List<Reservation> findByRoom(Room room);

    Optional<Reservation> findByReservationId(Long id);

    Optional<Reservation> findByClientsAndReservationId(Client client, Long reservationId);
}
