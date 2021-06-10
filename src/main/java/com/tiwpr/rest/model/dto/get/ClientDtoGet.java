package com.tiwpr.rest.model.dto.get;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.tiwpr.rest.model.dao.Client;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class ClientDtoGet {
    private long clientId;
    private String name;
    private String surname;
    private List<Long> reservations;

    public ClientDtoGet(Client client) {
        this.reservations = new ArrayList<>();
        this.setClientId(client.getClientId());
        this.setName(client.getName());
        this.setSurname(client.getSurname());
        client.getReservations().forEach(reservation ->
                this.getReservations().add(reservation.getReservationId()));
    }
}
