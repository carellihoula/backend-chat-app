package com.carel.backendapp.validation;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender javaMailSender;

    public void sendMail(Validation validation){
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setFrom("carel@no-reply.com");
        mail.setTo(validation.getUser().getEmail());
        mail.setSubject("Envoie de mail ");

        String name = validation.getUser().getName();
        String code = validation.getCode();
        String content = String.format("Bonjour %s, " +
                "voici le code pour valider votre compte: %s" ,name, code);
        mail.setText(content);

        javaMailSender.send(mail);
    }
}
