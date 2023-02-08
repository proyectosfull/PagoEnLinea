package com.revok.pagoEnLineaApi.controller;

import com.revok.pagoEnLineaApi.util.ContratoNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;


@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> methodNotFoundException() {
        return ResponseEntity.notFound().header("error", "Recurso no encontrado, revisa tu dirección y tipo de solicitud").build();
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errorMessages = new ArrayList<>();
        ex.getFieldErrors().forEach(fieldError -> errorMessages.add(fieldError.getField() + ": " +
                fieldError.getDefaultMessage() + ". " +
                "Valor recibido: " + fieldError.getRejectedValue()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<?> notBodyException() {
        return ResponseEntity.badRequest().header("error", "La solicitud no puede procesarse, posiblemente se deba a: " +
                "1.La solicitud no tiene cuerpo 2. los datos de los campos son inválidos para su tipo de dato " +
                "3. El cuerpo de la solicitud no es de tipo application/json").build();
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, NumberFormatException.class})
    public ResponseEntity<?> MismatchArgumentExceptionHandler(Exception ex) {
        return ResponseEntity.badRequest().header("error", "Los parametros no son válidos").build();
    }

    @ExceptionHandler(ContratoNotFound.class)
    public ResponseEntity<?> ProductNotAvailableExceptionHandler(ContratoNotFound exception) {
        return ResponseEntity.badRequest().header("error", exception.getMessage()).header("contrato", exception.getCvcontrato()).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> globalExceptionHandler(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.internalServerError().body(ex.getCause() == null ? "El servidor no añadió detalles"
                : ex.getCause().toString().split(":", 2)[1]);
    }
}
