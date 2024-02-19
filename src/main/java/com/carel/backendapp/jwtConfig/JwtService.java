package com.carel.backendapp.jwtConfig;

import com.carel.backendapp.user.User;
import com.carel.backendapp.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
public class JwtService {


    private  final UserRepository userRepository;

    @Value("${application.security.jwt.SECRET_KEY}")
    private  String SECRET_KEY;
    @Value("${application.security.jwt.expiration}")
    private Long expirationToken;
    @Value("${application.security.jwt.refreshToken.expiration}")
    private Long expirationRefreshToken;


    //extract username
    public String extractUsername(String token){
        return extract(token, Claims::getSubject);
    }

    // is token valid ??
    public boolean isTokenValid(String token, UserDetails userDetails){
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    //is token expired ??
    public boolean isTokenExpired(String token){
        Date expiration = extract(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    //Build extract one
    public <T> T extract(String token, Function<Claims, T> claims){
        Claims myClaims = extractAll(token);
        return claims.apply(myClaims);
    }

    //token
    public String generateToken(UserDetails userDetails){
        Map<String, Object> claims = new HashMap<>();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException("User doesn't exist")
        );
        claims.put("userId", user.getId());
        return generateToken(claims, userDetails);
    }
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
        return buildToken(extraClaims, userDetails, expirationToken);
    }

    //refresh_token
    public String generateRefreshToken(UserDetails userDetails){
        return buildToken(new HashMap<>(), userDetails, expirationRefreshToken);
    }
    //Build token
    public String buildToken(
            Map<String, Object> claims,
            UserDetails userDetails,
            long expiration
    ){
        return Jwts.builder()
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();

    }
    //Build extractAll
    public Claims extractAll(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    private Key getKey(){
        byte[] bytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(bytes);
    }
}
