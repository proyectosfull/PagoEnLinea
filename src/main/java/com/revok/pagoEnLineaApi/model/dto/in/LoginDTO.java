package com.revok.pagoEnLineaApi.model.dto.in;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
    @NotBlank
    private String fullname;
}
