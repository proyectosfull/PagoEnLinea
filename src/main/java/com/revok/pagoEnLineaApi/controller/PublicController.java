package com.revok.pagoEnLineaApi.controller;

import com.revok.pagoEnLineaApi.model.dto.out.PropietarioDTO;
import com.revok.pagoEnLineaApi.service.ContratoService;
import com.revok.pagoEnLineaApi.util.ContratoNotFound;
import com.revok.pagoEnLineaApi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {
    private final String BEARER = "Bearer ";
    private final ContratoService contratoService;
    private final JwtUtil jwtUtil;

    @GetMapping("findPropietario")
    public ResponseEntity<PropietarioDTO> findPropietario(@RequestParam(defaultValue = "") @NotBlank String cvcontrato) throws ContratoNotFound {
        String fullnameBlind = contratoService.findPropietarioByCvcontrato(cvcontrato);
        PropietarioDTO propietarioDTO = new PropietarioDTO();
        propietarioDTO.setFullname(fullnameBlind);
        return ResponseEntity.ok(propietarioDTO);
    }

    @GetMapping("refreshToken")
    public ResponseEntity<Void> refreshToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            try {
                String token = jwtUtil.getRefreshedToken(authorizationHeader.substring(BEARER.length()));
                return ResponseEntity.ok().header(AUTHORIZATION, BEARER + token).build();
            } catch (Exception e) {
                return ResponseEntity.badRequest().header("error", e.getMessage()).build();
            }
        } else {
            return ResponseEntity.badRequest()
                    .header("error", "Authorization header not present or not start with Bearer")
                    .build();
        }
    }
}
