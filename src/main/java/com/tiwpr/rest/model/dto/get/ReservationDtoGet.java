package com.tiwpr.rest.model.dto.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tiwpr.rest.model.dao.Hotel;
import com.tiwpr.rest.model.dao.Reservation;
import com.tiwpr.rest.repository.HotelRepository;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class ReservationDtoGet {
    private Long reservationId;
    private Long roomId;
    private Long hotelId;
    private List<Long> clientIds;
    private LocalDateTime date;

    public ReservationDtoGet(Reservation reservation, HotelRepository hotelRepository) {
        this.clientIds = new ArrayList<>();
        this.setReservationId(reservation.getReservationId());
        if (reservation.getDate() != null)
            this.setDate(reservation.getDate());
        if (reservation.getRoom() != null){
            this.setRoomId(reservation.getRoom().getRoomId());
            Optional<Hotel> hotelOpt = hotelRepository.findHotelByRooms(reservation.getRoom());
            if(hotelOpt.isPresent()){
                this.hotelId = hotelOpt.get().getHotelId();
            }
        }
        reservation.getClients().forEach(client ->
                this.getClientIds().add(client.getClientId()));
    }
}
