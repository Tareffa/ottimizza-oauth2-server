package br.com.ottimizza.application.services;

import java.text.MessageFormat;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import br.com.ottimizza.application.client.MailSenderClient;
import br.com.ottimizza.application.domain.dtos.MailDTO;
import br.com.ottimizza.application.model.user.User;
import lombok.Data;

@Service
public class MailServices {

    private TemplateEngine templateEngine;

    @Autowired
    private MailSenderClient mailSenderClient;
    
    @Value("${portal.server-url}")
    private String hostname;

    @Autowired
    public MailServices(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
    
    public String accountantInvitation(String invitationToken) {
        Context context = new Context();
        String registerURL = "";
        try {
            registerURL = new URIBuilder(MessageFormat.format("{0}/signup", hostname))
                    .addParameter("invitation_token", invitationToken).toString();
        } catch (Exception ex) {
        }
        context.setVariable("registerURL", registerURL);
        return templateEngine.process("mail/accountant_invitation", context);
    }

    public String inviteCustomerTemplate(User invitedBy, String registerToken) {
        Context context = new Context();
        String registerURL = "";
        try {
            registerURL = new URIBuilder(MessageFormat.format("{0}/signup", hostname))
                    .addParameter("invitation_token", registerToken).toString();
        } catch (Exception ex) {
        }
        context.setVariable("invitedBy", invitedBy);
        context.setVariable("registerURL", registerURL);
        return templateEngine.process("mail/invite_customer", context);
    }

    public void send(String to, String subject, String content) {
    	sendAws("", "", to, subject, content, "");
    }

    public void send(String name, String to, String subject, String content) {
    	sendAws("", name, to, subject, content, "");
    }

    public void send(String from, String name, String to, String subject, String content) {
    	sendAws("", name, to, subject, content, "");
    }

    public void send(String from, String name, String to, String subject, String content, String cc) {
    	sendAws("", name, to, subject, content, cc);
    }

    public void send(Builder messageBuilder) {
    	
    	sendAws("", "", messageBuilder.to, messageBuilder.subject, messageBuilder.content, "");
    }
    
    public void sendAws(String from, String name, String to, String subject, String content, String cc)  {

   	 MailDTO mail = new MailDTO();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setBody(content);
        mail.setName(name);
        mail.setCc(cc);
        
     	mail.setFrom(from); //liberar no SES algum diferente
        sendAwsSes(mail);
    }
    
    
    public void sendAwsSes(MailDTO mail) {
        try {
            System.out.println("enviando email");
            mailSenderClient.sendMail(mail);
        }
        catch(Exception ex) {
            System.out.println("caiu catch");
            System.out.println("message: "+ex.getMessage());
            System.err.println(ex.getStackTrace());
        }
    }
    
    @Data
    public static class Builder {

        private String from = "redefinicao@ottimizza.com.br";
        private String name;

        private String to;
        private String cc;
        private String bcc;
        private String replyTo;

        private String subject;
        private String content;
        private boolean htmlText;

        Builder() {
        }

        Builder withName(String name) {
            this.name = name;
            return this;
        }

        Builder withFrom(String from) {
            this.from = from;
            return this;
        }

        Builder withTo(String to) {
            this.to = to;
            return this;
        }

        Builder withReplyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        Builder withBcc(String bcc) {
            this.bcc = bcc;
            return this;
        }

        Builder withCc(String cc) {
            this.cc = cc;
            return this;
        }

        //
        Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        Builder withContent(String content, boolean htmlText) {
            this.content = content;
            this.htmlText = htmlText;
            return this;
        }

        Builder withHtml(String content) {
            return this.withContent(content, true);
        }

        Builder withText(String content) {
            return this.withContent(content, false);
        }

        MimeMessagePreparator build() {
            MimeMessagePreparator messagePreparator = mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
                if (this.name != null && !this.name.isEmpty()) {
                    messageHelper.setFrom(this.from, this.name);
                } else {
                    messageHelper.setFrom(this.from);
                }
                messageHelper.setTo(this.to);
                messageHelper.setSubject(this.subject);
                messageHelper.setText(this.content, this.htmlText);
                if (this.replyTo != null && !this.replyTo.isEmpty()) {
                    messageHelper.setReplyTo(this.replyTo);
                }
                if (this.cc != null && !this.cc.isEmpty()) {
                    messageHelper.setCc(this.cc);
                }
            };
            return messagePreparator;
        }

    }
    
}