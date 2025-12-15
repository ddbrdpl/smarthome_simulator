package cz.cvut.fel.omo.smarthome.people;

import cz.cvut.fel.omo.smarthome.house.Room;

public class Person {
    private final String id;
    private final String name;
    private final Role role;

    private Room location;
    private final PermissionSet permissions;

    public Person(String id, String name, Role role, Room location, PermissionSet permissions) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.location = location;
        this.permissions = permissions;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Role getRole() { return role; }

    public Room getLocation() { return location; }
    public void setLocation(Room location) { this.location = location; }

    public PermissionSet getPermissions() { return permissions; }
}
