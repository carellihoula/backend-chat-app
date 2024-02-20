package com.carel.backendapp.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //GET SPECIFIC USER
    @GetMapping(path="{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public User getUserInfo(
            @PathVariable("id") Integer id
    ){
        return userService.getUserInfo(id);
    }

    //GET ALL USERS
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public List<User> getAllUsers(){
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



}
