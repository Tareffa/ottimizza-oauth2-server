package br.com.ottimizza.application.client;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.ottimizza.application.domain.dtos.MailDTO;
import br.com.ottimizza.application.domain.responses.GenericResponse;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(name = "${email-sender.service.name}", url = "${email-sender.service.url}")
public interface MailSenderClient {


	@PostMapping(value = "/api/v1/emails", consumes = {"multipart/form-data"})
	HttpEntity<GenericResponse<?>> sendMail(@RequestBody MailDTO mailDto);	

}
