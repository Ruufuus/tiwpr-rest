package com.tiwpr.rest.model.dao;

import com.tiwpr.rest.model.dto.get.ClientDtoGet;
import com.tiwpr.rest.model.dto.post.ClientDtoPost;
import com.tiwpr.rest.repository.ReservationRepository;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Entity
public class Client {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long clientId;
    private String name;
    private String surname;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "Client_Reservation",
            joinColumns = { @JoinColumn(name = "client_id")},
            inverseJoinColumns = { @JoinColumn(name = "reservation_id")}
    )
    private List<Reservation> reservations;

    public Client(){
        this.reservations = new ArrayList<>();
    }


    public Client(ClientDtoGet clientDtoGet, ReservationRepository reservationRepository){
        this();
        this.clientId = clientDtoGet.getClientId();
        this.name = clientDtoGet.getName();
        this.surname = clientDtoGet.getSurname();
        clientDtoGet.getReservations().forEach(reservationDTO -> {
                Optional<Reservation> reservationOpt = reservationRepository.
                        findByReservationId(reservationDTO);
            reservationOpt.ifPresent(reservation -> this.reservations.add(reservation));
        });
    }


    public Client(ClientDtoPost clientDtoPost){
        this();
        this.name = clientDtoPost.getName();
        this.surname = clientDtoPost.getSurname();
        this.reservations = new ArrayList<>();
    }

    public void changeReservationList(List<Reservation> newReservations){
        this.reservations.removeAll(newReservations);
        this.reservations.forEach(this::removeClientFromReservation);
        this.reservations = newReservations;
        newReservations.forEach(reservation -> {
            if (!reservation.getClients().contains(this))
                reservation.getClients().add(this);
        });

    }


    public void removeClientFromReservations(){
        this.reservations.forEach(reservation -> {
            removeClientFromReservation(reservation);
        });
    }

    private void removeClientFromReservation(Reservation reservation) {
        reservation.getClients().remove(this);
        reservation.getClients().forEach(client -> {
            client.getReservations().forEach(reservation1 -> {
                if (reservation1.getClients().contains(this)){
                    reservation1.getClients().remove(this);
                }
            });
        });
    }
}
