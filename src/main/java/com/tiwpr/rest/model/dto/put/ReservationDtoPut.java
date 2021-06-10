package com.tiwpr.rest.model.dto.put;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tiwpr.rest.model.dao.Reservation;
import com.tiwpr.rest.model.dto.get.RoomDtoGet;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class ReservationDtoPut {
    @NotNull(message = "Room id must be set")
    private Long roomId;
    private List<Long> clientIds;
    @NotNull(message = "Reservation date must be set")
    private LocalDateTime date;

    public ReservationDtoPut(Reservation reservation){
        this.clientIds = new ArrayList<>();
        this.setDate(reservation.getDate());
        this.roomId = reservation.getRoom().getRoomId();
        }
}
