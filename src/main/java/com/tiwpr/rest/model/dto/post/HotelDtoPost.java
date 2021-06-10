package com.tiwpr.rest.model.dto.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tiwpr.rest.model.dao.Hotel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class HotelDtoPost {
    @NotBlank(message = "Hotel name must be filled")
    private String name;
    @NotBlank(message = "Hotel address must be filled")
    private String address;
    @NotNull(message = "Hotel star rating must be set")
    private Integer starRating;

    public HotelDtoPost(Hotel hotel) {
        this.setAddress(hotel.getAddress());
        this.setName(hotel.getName());
        this.setStarRating(hotel.getStarRating());
    }
}
