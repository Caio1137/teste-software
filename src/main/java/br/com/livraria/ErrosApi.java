package br.com.livraria;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
class ErrosApi {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException e) {
        var erro = e.getBindingResult().getFieldError();
        String mensagem = erro == null ? "Revise os dados do formulário." : erro.getDefaultMessage();
        return ResponseEntity.badRequest().body(Map.of("mensagem", mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> dadosInvalidos() {
        return ResponseEntity.badRequest().body(Map.of("mensagem", "Os dados enviados são inválidos."));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> resposta(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("mensagem", e.getReason()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> conflito() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensagem", "O registro já existe ou está ligado a outro cadastro."));
    }
}
