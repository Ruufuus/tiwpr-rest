package com.tiwpr.rest.model.dto.put;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tiwpr.rest.model.dao.Client;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class ClientDtoPut {
    @NotBlank(message = "Client name must be set")
    private String name;
    @NotBlank(message = "Client surname must be set")
    private String surname;
    @NotNull(message = "Client reservations must be set")
    private List<Long> reservations;

    public ClientDtoPut(Client client){
        this.setName(client.getName());
        this.setSurname(client.getSurname());
        client.getReservations().forEach(reservation -> this.reservations.add(reservation.getReservationId()));
    }
}
