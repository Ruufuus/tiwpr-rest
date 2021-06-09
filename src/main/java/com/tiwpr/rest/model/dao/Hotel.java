package com.tiwpr.rest.model.dao;

import com.tiwpr.rest.model.dto.get.HotelDtoGet;
import com.tiwpr.rest.model.dto.post.HotelDtoPost;
import com.tiwpr.rest.repository.RoomRepository;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Entity
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long hotelId;
    private String name;
    @Column(unique = true)
    private String address;
    private int starRating;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "hotel")
    private List<Room> rooms;

    public Hotel(){
        this.rooms = new ArrayList<>();
    }


    public Hotel(HotelDtoGet hotelDtoGet, RoomRepository roomRepository){
        this();
        this.hotelId = hotelDtoGet.getHotelId();
        this.name = hotelDtoGet.getName();
        this.address = hotelDtoGet.getAddress();
        this.starRating = hotelDtoGet.getStarRating();
        hotelDtoGet.getRoomsIds().forEach(roomId -> {
            Optional<Room> roomOpt = roomRepository.findById(roomId);
            roomOpt.ifPresent(room -> this.rooms.add(room));
        });
    }

    public Hotel(HotelDtoPost hotelDtoPost){
        this();
        this.name = hotelDtoPost.getName();
        this.address = hotelDtoPost.getAddress();
        this.starRating = hotelDtoPost.getStarRating();
        this.rooms = new ArrayList<>();
    }
}
