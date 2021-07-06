package br.com.ottimizza.application.controllers;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.ottimizza.application.domain.MessageDTO;
import br.com.ottimizza.application.domain.dtos.responses.GenericResponse;
import br.com.ottimizza.application.services.MessageService;

@RestController
@RequestMapping("/api/v1/message")
public class MessageController {

    @Inject
    MessageService service;

    @GetMapping
    public ResponseEntity<?> getMessage() throws Exception {
        return ResponseEntity.ok(new GenericResponse<MessageDTO>(
                service.getMessage()
            ));
    }
    
}
