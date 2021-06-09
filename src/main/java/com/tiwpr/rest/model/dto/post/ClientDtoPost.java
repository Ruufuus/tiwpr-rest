package com.tiwpr.rest.model.dto.post;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.tiwpr.rest.model.dao.Client;
import com.tiwpr.rest.model.dto.get.ReservationDtoGet;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class ClientDtoPost {
    @NotBlank(message = "Client name must be filled")
    private String name;
    @NotBlank(message = "Client surname must be filled")
    private String surname;

    public ClientDtoPost(Client client){
        this.setName(client.getName());
        this.setSurname(client.getSurname());
    }
}
