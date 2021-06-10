package com.tiwpr.rest.model.dto.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tiwpr.rest.model.dao.Room;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class RoomDtoGet {
    private long roomId;
    private List<Long> reservationsIds;
    private long hotelId;
    private String name;
    private Integer maxPeopleCount;
    private Integer price;

    public RoomDtoGet(Room room) {
        this.reservationsIds = new ArrayList<>();
        this.setRoomId(room.getRoomId());
        this.setMaxPeopleCount(room.getMaxPeopleCount());
        this.setName(room.getName());
        this.setPrice(room.getPrice());
        this.setHotelId(room.getHotel().getHotelId());
        room.getReservations().forEach(reservation ->
                this.getReservationsIds().add(reservation.getReservationId()));
    }
}
