package com.example.basichandlingexceptionvalidation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.basichandlingexceptionvalidation.dto.request.UserCreationRequest;
import com.example.basichandlingexceptionvalidation.dto.request.UserUpdateRequest;
import com.example.basichandlingexceptionvalidation.entity.User;
import com.example.basichandlingexceptionvalidation.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;



@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    //Create
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid UserCreationRequest userCreationRequest) {
        User createUser = userService.createUser(userCreationRequest);
        return new ResponseEntity<User>(createUser, HttpStatus.CREATED);
    }

    //Read
    @GetMapping
    public List<User> getAllUsers() {
        List<User> allUsers = userService.getAllUsers();
        return allUsers;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable("userId") String userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    //Update
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable("userId") String userId, @RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        User updateUser = userService.updateUser(userId, userUpdateRequest);
        return ResponseEntity.ok(updateUser);
    }

    //Delete
    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable("userId") String userId) {
        return userService.deleteUser(userId);
    }
}
