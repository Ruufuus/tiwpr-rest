package com.tiwpr.rest.model.dao;

import com.tiwpr.rest.model.dto.get.ReservationDtoGet;
import com.tiwpr.rest.model.dto.post.ReservationDtoPost;
import com.tiwpr.rest.repository.ClientRepository;
import com.tiwpr.rest.repository.RoomRepository;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public Reservation(){
        this.clients = new ArrayList<>();
    }

    public Reservation(ReservationDtoGet reservationDtoGet, RoomRepository roomRepository, ClientRepository clientRepository){
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
    public Reservation(ReservationDtoPost reservationDtoPost, long clientId, RoomRepository roomRepository, ClientRepository clientRepository){
        this();
        this.date = reservationDtoPost.getDate();
        Optional<Room> roomOpt = roomRepository.findById(reservationDtoPost.getRoomId());
        roomOpt.ifPresent(value -> this.room = value);
        reservationDtoPost.getClientIds().add(clientId);
        reservationDtoPost.getClientIds().forEach(id -> {
            Optional<Client> clientOpt = clientRepository.findByClientId(id);
            if(clientOpt.isPresent()){
                this.clients.add(clientOpt.get());
                clientOpt.get().getReservations().add(this);
            };
        });
    }
}
