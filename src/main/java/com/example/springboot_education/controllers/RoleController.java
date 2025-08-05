package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.roleDTOs.CreateRoleRequestDto;
import com.example.springboot_education.dtos.roleDTOs.RoleResponseDto;
import com.example.springboot_education.dtos.roleDTOs.UpdateRoleRequestDto;
import com.example.springboot_education.dtos.usersDTOs.UserIdsRequestDto;
import com.example.springboot_education.entities.Role;
import com.example.springboot_education.services.RoleService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/security/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping()
    public Role create(@RequestBody @Valid CreateRoleRequestDto data) {
        return roleService.create(data);
    }

    @PatchMapping("/{id}")
    public Role update(@PathVariable("id") Integer id, @RequestBody @Valid UpdateRoleRequestDto data) {
        return roleService.update(id, data);
    }

    // @GetMapping()
    // public List<RoleResponseDto> getAllRole() {
    //     return roleService.getAllRole();
    // }

    @GetMapping
    public Iterable<Role> getAllRoles() {
    return roleService.getRoles();
    }

        @GetMapping("/{id}")
public ResponseEntity<Role> getRoleById(@PathVariable("id") Integer id) {
    Role role = roleService.findById(id);
    return ResponseEntity.ok(role);
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteRole(@PathVariable("id") Integer id) {
    roleService.delete(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
}

    @PatchMapping("/{id}/add-users-to-role")
    public ResponseEntity<String> addUsersToRole(@PathVariable("id") Integer id,
            @RequestBody UserIdsRequestDto request) {
        roleService.addUsersToRole(id, request.getUserIds());
        return ResponseEntity.ok("Users added to role successfully!");
    }

    @DeleteMapping("/{id}/remove-users-from-role")
    public ResponseEntity<String> removeUsersFromRole(@PathVariable("id") Integer id,
            @RequestBody com.example.springboot_education.dtos.usersDTOs.UserIdsRequestDto request) {
        roleService.removeUsersFromRole(id, request.getUserIds());
        return ResponseEntity.ok("Users removed from role successfully!");
    }


}