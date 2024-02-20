package com.carel.backendapp.validation;

import com.carel.backendapp.user.User;
import com.carel.backendapp.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;

@Service
@AllArgsConstructor

public class ValidationService {
    private UserRepository userRepository;
    private NotificationService notif;
    private ValidationRepository validationRepository;

    public void validation(User user){

        Instant creation = Instant.now();
        Instant expiration = creation.plus(10, ChronoUnit.MINUTES);

        Random random = new Random();
        int alea = random.nextInt(999999);
        String code = String.format("%06d", alea);

        Validation validation = Validation.builder()
                .createdAt(creation)
                .expiresAt(expiration)
                .user(user)
                .code(code)
                .build();
        validationRepository.save(validation);
        notif.sendMail(validation);
    }

    public Validation readByCode(String code){
        return validationRepository.findByCode(code).orElseThrow(
                () -> new RuntimeException("Code invalide")
        );
    }
}
