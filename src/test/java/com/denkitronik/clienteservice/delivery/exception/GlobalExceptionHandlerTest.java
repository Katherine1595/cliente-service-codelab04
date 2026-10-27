package com.denkitronik.clienteservice.delivery.exception;

import com.denkitronik.clienteservice.domain.exception.ClienteNotFoundException;
import com.denkitronik.clienteservice.domain.exception.ClienteServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit tests — GlobalExceptionHandler y excepciones del dominio")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("ClienteNotFoundException → 404 con el mensaje de la excepción")
    void manejarNotFound_debeRetornar404() {
        ResponseEntity<ApiErrorResponse> respuesta =
                handler.manejarNotFound(new ClienteNotFoundException(7L));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody().getStatus()).isEqualTo(404);
        assertThat(respuesta.getBody().getError()).isEqualTo("Not Found");
        assertThat(respuesta.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("ClienteServiceException → 500 con mensaje genérico")
    void manejarServiceException_debeRetornar500() {
        ResponseEntity<ApiErrorResponse> respuesta =
                handler.manejarServiceException(new ClienteServiceException("fallo"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(respuesta.getBody().getMensaje())
                .isEqualTo("Error interno del servidor. Por favor intenta más tarde.");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException → 400 con campo y mensaje")
    void manejarValidacion_debeRetornar400ConErrores() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "cliente");
        binding.addError(new FieldError("cliente", "nombre", "no puede estar vacío"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(
                        GlobalExceptionHandlerTest.class.getDeclaredMethod(
                                "metodoAuxiliar", String.class), 0),
                binding);

        ResponseEntity<Map<String, String>> respuesta = handler.manejarValidacion(ex);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).containsEntry("nombre", "no puede estar vacío");
    }

    @Test
    @DisplayName("Exception genérica → 500 con mensaje de error inesperado")
    void manejarExcepcionGenerica_debeRetornar500() {
        ResponseEntity<ApiErrorResponse> respuesta =
                handler.manejarExcepcionGenerica(new IllegalStateException("inesperado"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(respuesta.getBody().getMensaje())
                .isEqualTo("Error inesperado. Por favor contacta al administrador.");
    }

    @Test
    @DisplayName("ClienteServiceException conserva mensaje y causa")
    void clienteServiceException_conCausa_debeConservarla() {
        RuntimeException causa = new RuntimeException("origen");
        ClienteServiceException ex = new ClienteServiceException("mensaje", causa);

        assertThat(ex.getMessage()).isEqualTo("mensaje");
        assertThat(ex.getCause()).isSameAs(causa);
    }

    @SuppressWarnings("unused")
    private void metodoAuxiliar(String parametro) {
        // solo sirve para construir el MethodParameter de la excepción
    }
}
