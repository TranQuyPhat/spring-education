package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.usersDTOs.CreateUserRequestDto;
import com.example.springboot_education.dtos.usersDTOs.UpdateUserRequestDto;
import com.example.springboot_education.dtos.usersDTOs.UserResponseDto;
import com.example.springboot_education.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public List<UserResponseDto> getAllUsers() {
        return userService.getUsers();
    }

    

    // GET USER BY ID
    @GetMapping("/{id}")
    public UserResponseDto getUserById(@PathVariable("id") Integer id) {
        return this.userService.getUserById(id);
    }

    // CREATE NEW USER
    @PostMapping()
    public UserResponseDto createUser(@RequestBody @Valid CreateUserRequestDto createUserRequestDto) {
        return this.userService.createUser(createUserRequestDto);
    }

    // UPDATE USER BY ID
    @PatchMapping("/{id}")
    public UserResponseDto updateUser(@PathVariable("id") Integer id, @RequestBody UpdateUserRequestDto dto) {
        return this.userService.updateUser(id, dto);
    }

    // DELETE USER BY ID
  @DeleteMapping("/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable("id") Integer id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build(); 
}

}