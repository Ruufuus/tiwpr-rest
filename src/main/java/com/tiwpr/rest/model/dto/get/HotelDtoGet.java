package com.tiwpr.rest.model.dto.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tiwpr.rest.model.dao.Hotel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class HotelDtoGet {
    private long hotelId;
    private String name;
    private String address;
    private int starRating;
    private List<Long> roomsIds;

    public HotelDtoGet(Hotel hotel){
        this.roomsIds = new ArrayList<>();
        this.setHotelId(hotel.getHotelId());
        this.setAddress(hotel.getAddress());
        this.setName(hotel.getName());
        hotel.getRooms().forEach(room ->
                this.roomsIds.add(room.getRoomId()));
        this.setStarRating(hotel.getStarRating());
    }
}
