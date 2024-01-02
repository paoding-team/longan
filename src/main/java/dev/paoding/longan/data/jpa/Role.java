package dev.paoding.longan.data.jpa;

public class Role {
    private String name;

    public static Role of(String name) {
        Role role = new Role();
        role.name = name;
        return role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
