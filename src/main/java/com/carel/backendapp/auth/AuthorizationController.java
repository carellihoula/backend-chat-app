package com.carel.backendapp.auth;

import com.carel.backendapp.user.User;
import com.carel.backendapp.user.UserRepository;
import com.carel.backendapp.validation.Validation;
import com.carel.backendapp.validation.ValidationRepository;
import com.carel.backendapp.validation.ValidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthorizationController {

    private AuthorizationService authorizationService;
    private ValidationService validationService;
    private ValidationRepository validationRepository;
    private UserRepository userRepository;
    private LogoutService logoutService;

    @PostMapping(path="/register",consumes= MediaType.APPLICATION_JSON_VALUE)
    public void register(@RequestBody User user){
         authorizationService.register(user);
    }

    @PostMapping(path="/login",consumes= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthorizationResponse> authentication(@RequestBody AuthorizationRequest request){
        AuthorizationResponse authentication = authorizationService.authentication(request);
        return ResponseEntity.ok(authentication);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            HttpServletResponse  response
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logoutService.logout(request, response, authentication);

        return ResponseEntity.status(HttpStatus.OK).body("logout successfully");
    }

    @PostMapping(path="/activate")
    public void authentication(@RequestBody Map<String, String> activation){
        Validation validation = validationService.readByCode(activation.get("code"));

        if(validation.getExpiresAt().isBefore(Instant.now())){
            throw new RuntimeException("votre code est expiré");
        }
        if(!validation.getCode().equals(activation.get("code"))){
            throw new RuntimeException("votre code n'est pas le bon " +
                    "===> code Incorrect, veuillez verifier votre mail");
        }

        User user = userRepository.findByEmail(validation.getUser().getEmail()).orElseThrow(
                () -> new RuntimeException("User not found")
        );
        validation.setActivation(Instant.now());
        user.setActive(true);
        userRepository.save(user);
        validationRepository.save(validation);

    }

    @PostMapping("/refresh-token")
    public void refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        authorizationService.refresh(request, response);
    }

}
