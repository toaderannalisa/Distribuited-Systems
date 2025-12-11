package com.example.demo.dtos;

import com.example.demo.entities.PersonRole;
import java.util.Objects;
import java.util.UUID;

public class PersonDTO {
    private UUID id;
    private String name;
    private String username;
    private int age;
    private PersonRole role;
    private String  address;

    public PersonDTO() {}
    public PersonDTO(UUID id, String name, String username, int age, PersonRole role, String address) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.age = age;
        this.role = role;
        this.address = address;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public PersonRole getRole() { return role; }
    public void setRole(PersonRole role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonDTO that = (PersonDTO) o;
        return age == that.age &&
                Objects.equals(name, that.name) &&
                Objects.equals(username, that.username);
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }


    @Override
    public int hashCode() {
        return Objects.hash(name, username, age);
    }
}