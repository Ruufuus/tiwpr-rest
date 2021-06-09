package com.tiwpr.rest.model.dao;

import com.tiwpr.rest.model.dto.get.RoomDtoGet;
import com.tiwpr.rest.model.dto.post.RoomDtoPost;
import com.tiwpr.rest.repository.HotelRepository;
import com.tiwpr.rest.repository.ReservationRepository;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Data
@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long roomId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "room")
    private List<Reservation> reservations;
    @JoinColumn(name = "hotelId")
    @ManyToOne(cascade = CascadeType.DETACH)
    private Hotel hotel;
    private String name;
    private Integer maxPeopleCount;
    private Integer price;

    public Room(){
        this.reservations = new ArrayList<>();
    }

    public Room(RoomDtoGet roomDtoGet, HotelRepository hotelRepository, ReservationRepository reservationRepository){
        this();
        this.roomId = roomDtoGet.getRoomId();
        this.name = roomDtoGet.getName();
        this.maxPeopleCount = roomDtoGet.getMaxPeopleCount();
        this.price = roomDtoGet.getPrice();
        roomDtoGet.getReservationsIds().forEach(id -> {
            Optional<Reservation> reservationOpt = reservationRepository.findById(id);
            reservationOpt.ifPresent(value -> this.reservations.add(value));
        });
        Optional<Hotel> hotelOpt = hotelRepository.findByHotelId(roomDtoGet.getHotelId());
        hotelOpt.ifPresent(value -> this.hotel = value);
    }

    public Room(RoomDtoPost roomDtoPost, long hotelId, HotelRepository hotelRepository){
        this();
        this.name = roomDtoPost.getName();
        this.maxPeopleCount = roomDtoPost.getMaxPeopleCount();
        this.price = roomDtoPost.getPrice();
        Optional<Hotel> hotelOpt = hotelRepository.findByHotelId(hotelId);
        hotelOpt.ifPresent(value -> this.hotel = value);
    }
}
