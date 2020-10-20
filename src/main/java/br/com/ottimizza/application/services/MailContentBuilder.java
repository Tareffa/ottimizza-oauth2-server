package br.com.ottimizza.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailContentBuilder {

  private TemplateEngine templateEngine;

  @Autowired
  public MailContentBuilder(TemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }

  public String build(String greeting, String action) {
    Context context = new Context();
    context.setVariable("greeting", greeting);
    context.setVariable("action", action);
    return templateEngine.process("mail/password_reset", context);
  }

}