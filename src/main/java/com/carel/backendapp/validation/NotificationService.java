package com.carel.backendapp.validation;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender javaMailSender;
    @Value("${spring.profiles.active}")
    private String profile;
    public void sendMail(Validation validation) throws MessagingException {
        //SimpleMailMessage mail = new SimpleMailMessage();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom("carel@no-reply.com");
        helper.setTo(validation.getUser().getEmail());
        helper.setSubject("Envoie de mail ");
        String activationUrl = "https://canoappchat.vercel.app/activate";
        String name = validation.getUser().getUsername();
        String code = validation.getCode();

        String content = String.format("<html>" +
                "<body>" +
                "<p>Bonjour %s,</p>" +
                "<p>Voici le code pour valider votre compte: <strong>%s</strong></p>" +
                "<p>Vous pouvez également activer votre compte en cliquant sur le lien suivant: " +
                "<a href=\"%s\">Activer Mon Compte</a></p>" +
                "</body>" +
                "</html>", name, code, activationUrl);
        helper.setText(content, true);

        javaMailSender.send(mimeMessage);
    }
}
