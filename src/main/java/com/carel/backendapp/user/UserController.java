package com.carel.backendapp.user;

import com.carel.backendapp.aws.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final StorageService storageService;

    //GET SPECIFIC USER
    @GetMapping(path="{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UserResponse getUserInfo(
            @PathVariable("id") Integer id
    ){
        return userService.getUserInfo(id);
    }

    //GET ALL USERS
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public List<UserResponse> getAllUsers(){
        return userService.getAllUsers();
    }

    //Update User
    @PutMapping("{id}")
    public void updateUser(
            @PathVariable("id") Integer id,
            @RequestBody User user
    ){
        userService.updateAndSaveUser(id, user);
    }

    //DeleteUser
    @DeleteMapping(path="{id}")
    public void deleteUser(
          @PathVariable("id") Integer id
    ){
        userService.deleteUser(id);
    }

    //Delete multi User
    @DeleteMapping(path="/mutli-delete")
    public void deleteMultiUser(
             @RequestBody List<User> users
    ){
        userService.deleteMulti(users);
    }

    @PostMapping("/upload/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> uploadFileToAws(
            @PathVariable("id") Integer id,
            @RequestParam("file") MultipartFile file
            ) throws IOException {

            String fileUrl = storageService.uploadFileToAws(id, file);
            return ResponseEntity.ok(fileUrl);
    }

    ///////////////////////////////CHAT/////////////////////////////////
    @MessageMapping("/user.addUser")
    @SendToUser("/user/topic")
    public User addUser(
          @Payload User user
    ){
        return userService.saveUser(user);
    }
    // disconnect user
    @MessageMapping("/user.disconnect")
    @SendToUser("/user/topic")
    public User disconnect(
            @Payload User user
    ){
        userService.disconnect(user);
        return user;
    }
    //find users connected
    @GetMapping("/connected")
    public ResponseEntity<List<User>> findConnectedUSer(

    ){
        return ResponseEntity.ok(userService.getConnectedUser());
    }

}
