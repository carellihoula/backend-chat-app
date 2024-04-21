package com.carel.backendapp.auth;

import com.carel.backendapp.jwtConfig.JwtService;
import com.carel.backendapp.token.Token;
import com.carel.backendapp.token.TokenRepository;
import com.carel.backendapp.user.Role;
import com.carel.backendapp.user.User;
import com.carel.backendapp.user.UserRepository;
import com.carel.backendapp.validation.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


@Transactional
@Service
@AllArgsConstructor
public class AuthorizationService {

    private UserRepository userRepository;
    private TokenRepository tokenRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private ValidationService validationService;

    private static final Pattern emailPattern = Pattern.compile(
            "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"
    );

    public void register(User user) throws MessagingException {
        //exception if user exists
        Optional<User> userfound = userRepository.findByEmail(user.getEmail());
        if(userfound.isPresent()){
            throw new RuntimeException("user exists already");
        }
        //Encrypt password
        String passwordEnc = passwordEncoder.encode(user.getPassword());
        user.setPassword(passwordEnc);
        //valid Email
        if(!isEmailValid(user.getEmail())){
            throw new IllegalArgumentException("Format email invalid");
        };

        user.setRole(Role.USER);
        user.setActive(false);
        userRepository.save(user);
        validationService.validation(user);
    }

    //authenticate user with login(email) and password
    public AuthorizationResponse authentication(AuthorizationRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new UsernameNotFoundException("User Not Found ==> from authentication methode")
        );
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        removeUselessToken(user);
        //stock le refresh dans la base de données
        userTokenSaved(user, refreshToken);
        return AuthorizationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }

    //generate token jwt when user is authenticated
    public void userTokenSaved(User user, String token){
        Token tokenUser = Token.builder()
                .expired(false)
                .revoked(false)
                .token(token)
                .user(user)
                .build();
        tokenRepository.save(tokenUser);
    }
    //remove all user token when user is authenticated and keep the last one.
    public void removeUselessToken(User user){
        List<Token> allValidTokenUser = tokenRepository.findAllValidTokenUser(user.getEmail());
        if(allValidTokenUser.isEmpty()){
            return;
        }
        allValidTokenUser.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(allValidTokenUser);
        tokenRepository.deleteAllByExpiredAndRevoked(true, true);
    }
   // verify if email respect email pattern
    public static boolean isEmailValid(String email) {
        if(email == null){
            return false;
        }
        return emailPattern
                .matcher(email)
                .matches();
    }

    //refresh token to improve user experience
    public void refresh(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        final Token refreshInDB;

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);//extract userEmail from token;

        if(!jwtService.isRefreshToken(refreshToken)){
             throw new RuntimeException("il ne s'agit pas d'un refresh token");
        }
        if(userEmail != null){
            var user = userRepository.findByEmail(userEmail).orElseThrow(
                    () -> new UsernameNotFoundException("user not found")
            );
            if(jwtService.isTokenValid(refreshToken, user)){
                var accessToken = jwtService.generateToken(user);
                var refreshResponse = RefreshResponse.builder()
                        .accessToken(accessToken)
                        .build();
                //Bibliothèque jackson
                new ObjectMapper().writeValue(response.getOutputStream(), refreshResponse);
            }
        }
    }

}
