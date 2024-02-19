package com.carel.backendapp.auth;

import com.carel.backendapp.user.User;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthorizationController {

    private AuthorizationService authorizationService;

    @PostMapping(path="/register",consumes= MediaType.APPLICATION_JSON_VALUE)
    public void register(@RequestBody User user){
         authorizationService.register(user);
    }

    @PostMapping(path="/login",consumes= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthorizationResponse> authentication(@RequestBody AuthorizationRequest request){
        AuthorizationResponse authentication = authorizationService.authentication(request);
        return ResponseEntity.ok(authentication);
    }
}
