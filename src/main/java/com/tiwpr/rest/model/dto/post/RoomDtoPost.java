package com.tiwpr.rest.model.dto.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tiwpr.rest.model.dao.Room;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class RoomDtoPost {
    @NotBlank(message = "Room name must be set")
    private String name;
    @NotNull(message = "Room maximum capacity must be set")
    private Integer maxPeopleCount;
    @NotNull(message = "Room price must be set")
    private Integer price;

    public RoomDtoPost(Room room) {
        this.setMaxPeopleCount(room.getMaxPeopleCount());
        this.setName(room.getName());
        this.setPrice(room.getPrice());
    }
}
