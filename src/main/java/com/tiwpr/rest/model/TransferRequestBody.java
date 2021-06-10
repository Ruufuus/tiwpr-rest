package com.tiwpr.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class TransferRequestBody {
    @NotNull(message = "Old room id must be set")
    Long currentRoomId;
    @NotNull(message = "New room id must be set")
    Long newRoomId;
}
