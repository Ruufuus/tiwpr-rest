package com.tiwpr.rest.model.dao;

import com.tiwpr.rest.model.dto.get.ReservationDtoGet;
import com.tiwpr.rest.model.dto.put.ReservationDtoPut;
import com.tiwpr.rest.repository.ClientRepository;
import com.tiwpr.rest.repository.RoomRepository;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long reservationId;
    @JoinColumn(name = "roomId")
    @ManyToOne(cascade = CascadeType.DETACH)
    private Room room;
    @ManyToMany(mappedBy = "reservations")
    private List<Client> clients;
    private LocalDateTime date;

    public Reservation() {
        this.clients = new ArrayList<>();
    }

    public Reservation(ReservationDtoGet reservationDtoGet, RoomRepository roomRepository, ClientRepository clientRepository) {
        this();
        this.reservationId = reservationDtoGet.getReservationId();
        this.date = reservationDtoGet.getDate();
        Optional<Room> roomOpt = roomRepository.findById(reservationDtoGet.getRoomId());
        roomOpt.ifPresent(value -> this.room = value);
        reservationDtoGet.getClientIds().forEach(id -> {
            Optional<Client> clientOpt = clientRepository.findByClientId(id);
            clientOpt.ifPresent(client -> this.clients.add(client));
        });
    }

    public Reservation(ReservationDtoPut reservationDtoPut, long clientId, RoomRepository roomRepository, ClientRepository clientRepository) {
        this();
        this.date = reservationDtoPut.getDate();
        Optional<Room> roomOpt = roomRepository.findById(reservationDtoPut.getRoomId());
        roomOpt.ifPresent(value -> this.room = value);
        reservationDtoPut.getClientIds().add(clientId);
        reservationDtoPut.getClientIds().forEach(id -> {
            Optional<Client> clientOpt = clientRepository.findByClientId(id);
            if (clientOpt.isPresent()) {
                this.clients.add(clientOpt.get());
                clientOpt.get().getReservations().add(this);
            }
        });
    }
}
