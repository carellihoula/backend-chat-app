package com.carel.backendapp.validation;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender javaMailSender;
    @Value("${spring.profiles.active}")
    private String profile;
    public void sendMail(Validation validation){
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setFrom("carel@no-reply.com");
        mail.setTo(validation.getUser().getEmail());
        mail.setSubject("Envoie de mail ");
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
        mail.setText(content);

        javaMailSender.send(mail);
    }
}
