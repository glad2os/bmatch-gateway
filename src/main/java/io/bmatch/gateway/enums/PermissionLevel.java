package io.bmatch.gateway.enums;

import lombok.Getter;

@Getter
public enum PermissionLevel {
    ADMINISTRATOR("administrator"),
    PREMIUM("premium"),
    PLAYER("player");

    private final String permission;

    PermissionLevel(String permission) {
        this.permission = permission;
    }
}

