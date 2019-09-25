package br.com.ottimizza.application.services;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import br.com.ottimizza.application.model.user.User;
import lombok.Data;

@Service
public class MailServices {

    private TemplateEngine templateEngine;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${hostname:https://accounts.ottimizza.com.br}")
    private String hostname;

    @Autowired
    public MailServices(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    private String passwordReset(String fullname, String passwordResetURL) {
        Context context = new Context();
        context.setVariable("fullname", fullname);
        context.setVariable("passwordResetURL", passwordResetURL);
        return templateEngine.process("mail/password_reset", context);
    }

    public String inviteCustomerTemplate(User invitedBy, String registerToken) {
        Context context = new Context();
        String registerURL = "";
        try {
            System.out.println(hostname);
            registerURL = new URIBuilder("https://accounts.ottimizza.com.br/register")
                    .addParameter("token", registerToken).toString();
        } catch (Exception ex) {
        }
        context.setVariable("invitedBy", invitedBy);
        context.setVariable("registerURL", registerURL);
        return templateEngine.process("mail/invite_customer", context);
    }

    public void send(String to, String subject, String content) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("redefinicao@depaula.com.br");
            messageHelper.setReplyTo("lucas@ottimizza.com.br");
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
        };
        mailSender.send(messagePreparator);
    }

    public void send(String name, String to, String subject, String content) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("redefinicao@ottimizza.com.br", name);
            messageHelper.setReplyTo("lucas@ottimizza.com.br");
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
        };
        mailSender.send(messagePreparator);
    }

    public void send(String from, String name, String to, String subject, String content) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            String userDomain = from;

            if (from.indexOf("@") >= 0) {
                userDomain = from.substring(from.lastIndexOf("@") + 1);
            }

            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(MessageFormat.format(MessageFormat.format("account@{0}", userDomain), from), name);
            messageHelper.setReplyTo("lucas@ottimizza.com.br");
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
        };
        mailSender.send(messagePreparator);
    }

    public void send(String from, String name, String to, String subject, String content, String cc) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            String userDomain = from;

            if (from.indexOf("@") >= 0) {
                userDomain = from.substring(from.lastIndexOf("@") + 1);
            }

            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(MessageFormat.format(MessageFormat.format("account@{0}", userDomain), from), name);
            messageHelper.setReplyTo("lucas@ottimizza.com.br");
            messageHelper.setTo(to);
            messageHelper.setCc(cc);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
        };
        mailSender.send(messagePreparator);
    }

    public void send(Builder messageBuilder) {
        mailSender.send(messageBuilder.build());
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