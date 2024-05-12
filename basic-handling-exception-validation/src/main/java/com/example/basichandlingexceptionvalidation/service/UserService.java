package com.example.basichandlingexceptionvalidation.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.basichandlingexceptionvalidation.dto.request.UserCreationRequest;
import com.example.basichandlingexceptionvalidation.dto.request.UserUpdateRequest;
import com.example.basichandlingexceptionvalidation.entity.User;
import com.example.basichandlingexceptionvalidation.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    //Create
    public User createUser(UserCreationRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username existed.");
        }

        User newUser = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(request.getPassword())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .dob(request.getDob())
            .build();

        return userRepository.save(newUser);
    }

    //Read
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found."));
    }

    //Update
    public User updateUser(String userId, UserUpdateRequest request) {
        User updateUser = getUserById(userId);

        updateUser.setPassword(request.getPassword());
        updateUser.setFirstName(request.getFirstName());
        updateUser.setLastName(request.getLastName());
        updateUser.setDob(request.getDob());

        return userRepository.save(updateUser);
    }

    //Delete
    public String deleteUser(String userId) {
        userRepository.deleteById(userId);
        return "User has been deleted.";
    }
}
