package com.carel.backendapp.auth;

import com.carel.backendapp.jwtConfig.JwtService;
import com.carel.backendapp.token.Token;
import com.carel.backendapp.token.TokenRepository;
import com.carel.backendapp.user.Role;
import com.carel.backendapp.user.User;
import com.carel.backendapp.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
@AllArgsConstructor
public class AuthorizationService {

    private UserRepository userRepository;
    private TokenRepository tokenRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private static final Pattern emailPattern = Pattern.compile(
            "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"
    );

    public void register(User user) {
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
    }

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
        userTokenSaved(user, refreshToken);
        return AuthorizationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }

    public void userTokenSaved(User user, String token){
        Token tokenUser = Token.builder()
                .expired(true)
                .revoked(true)
                .token(token)
                .user(user)
                .build();
        tokenRepository.save(tokenUser);
    }
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
        tokenRepository.DeleteAllByExpiredAndRevoked(true, true);
    }

    public static boolean isEmailValid(String email) {
        if(email == null){
            return false;
        }
        return emailPattern
                .matcher(email)
                .matches();
    }

}
