package com.carel.backendapp.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    //get specific user
    public User getUserInfo(Integer id) {
        return userRepository.findById(id).orElseThrow(
                () -> new RuntimeException("User doesn't exist")
        );
    }
    // get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
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
}
