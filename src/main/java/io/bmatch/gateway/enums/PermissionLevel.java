package io.bmatch.gateway.enums;

import lombok.Getter;

@Getter
public enum PermissionLevel {
    ADMINISTRATOR("administrator"),
    PLAYER("viewer");

    private final String permission;

    PermissionLevel(String permission) {
        this.permission = permission;
    }

}

