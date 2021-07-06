package br.com.ottimizza.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.ottimizza.application.domain.MessageDTO;

@Service
public class MessageService {
    
    @Value("${message}")
    private String MESSAGE_ALERT;

    public MessageDTO getMessage() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MessageDTO message = mapper.readValue(MESSAGE_ALERT, MessageDTO.class);
        return message;
    }
}
