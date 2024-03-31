package com.carel.backendapp.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    //get specific user
    public UserResponse getUserInfo(Integer id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new RuntimeException("User doesn't exist")
        );
        return saveUserResponse(user);

    }
    public UserResponse saveUserResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .profileImage(user.getProfileImage())
                .active(user.isActive())
                .build();
    }
    // get all users
    public List<UserResponse> getAllUsers() {
        List<User> userList = userRepository.findAll();
        return userList.stream().map(this::saveUserResponse).toList();
    }

    //delete specific user
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
    //Multi delete
    public void deleteMulti(List<User> users) {
        userRepository.deleteAllInBatch(users);
    }
    //update User
    public void updateAndSaveUser(Integer id, User user){
        User storedUser = userRepository.findByEmail(user.getEmail()).orElseThrow(
                () -> new RuntimeException("User not found")
        );
        storedUser.setName(user.getName());
        storedUser.setPassword(user.getPassword());
        storedUser.setEmail(user.getEmail());
        storedUser.setProfileImage(user.getProfileImage());
        userRepository.save(storedUser);
    }

    //////////////////////////CHAT/////////////////////////////////////////
    //save user
    public User saveUser(User user){
        user.setStatus(Status.ONLINE);
        userRepository.save(user);
        return user;
    }

    //disconnect user
    public void disconnect(User user){
        User storedUser = userRepository.findByEmail(user.getEmail()).orElse(null);
        if(storedUser != null){
            storedUser.setStatus(Status.OFFLINE);
            userRepository.save(storedUser);
        }

    }

    //findUser connected
    public List<User> getConnectedUser(){
         return userRepository.findAllByStatus(Status.ONLINE);
    }

}
