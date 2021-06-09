package com.tiwpr.rest.model.dto.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tiwpr.rest.model.dao.Reservation;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class ReservationDtoGet {
    private long reservationId;
    private long roomId;
    private List<Long> clientIds;
    private LocalDateTime date;

    public ReservationDtoGet(Reservation reservation){
        this.clientIds = new ArrayList<>();
        this.setReservationId(reservation.getReservationId());
        this.setDate(reservation.getDate());
        this.setRoomId(reservation.getRoom().getRoomId());
    reservation.getClients().forEach(client ->
            this.getClientIds().add(client.getClientId()));
    }
}
