package com.example.springboot_education.events;

import org.springframework.context.ApplicationEvent;

public class RoleUnassignedEvent extends ApplicationEvent {
    private final Integer userId;
    private final Integer roleId;

    public RoleUnassignedEvent(Integer userId, Integer roleId) {
        super(userId);
        this.userId = userId;
        this.roleId = roleId;
    }

    public Integer getUserId() { return userId; }
    public Integer getRoleId() { return roleId; }
}
