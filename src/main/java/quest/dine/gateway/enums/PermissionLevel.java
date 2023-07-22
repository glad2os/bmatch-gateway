package quest.dine.gateway.enums;

public enum PermissionLevel {
    ADMINISTRATOR("administrator"),
    EDITOR("editor"),
    VIEWER("viewer");

    private final String permission;

    PermissionLevel(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}

