package br.com.ottimizza.application.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailDTO {
	private String to;
	
	private String subject;
	
	private String body;
	
}
